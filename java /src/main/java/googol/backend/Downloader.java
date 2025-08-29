package googol.backend;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.net.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;


/**
 * Classe que representa o Downloader, responsável por fazer o download de páginas web e enviar para os Barrels
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class Downloader {

	/**
	 * Temporizador para atualizar a lista de Barrels
	 */
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Lista de Barrels
	 */
	private List<BarrelInterface> barrels = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Representa a conexão com o Gateway
	 */
	private GatewayInterface gateway;

	/**
	 * Número do downloader
	 */
	private final int downloaderNumber;

	/**
	 * Número de tentativas de envio de informação para o barrel
	 */
	private int totalAttemps = 5;


	/**
	 * Lista de URLs visitados
	*/
    private static Set<String> visitedUrls = ConcurrentHashMap.newKeySet();


	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * @param gateway Interface do Gateway
	 * @throws RemoteException
	*/
	public Downloader(GatewayInterface gateway) throws RemoteException {
		super();
		this.gateway = gateway;
		this.downloaderNumber = gateway.registerDownloader();
		this.barrels = new ArrayList<>();
		updateBarrels();
		scheduler.scheduleAtFixedRate(this::updateBarrels, 2, 2, TimeUnit.SECONDS);
		try {
			// Obtém os Barrels registrados no Gateway
			this.barrels = gateway.getRegisteredBarrels();
			System.out.println("Downloader " + downloaderNumber + " Started");
			System.out.println("Barrels obtidos com sucesso: " + this.barrels.size());
		} catch (RemoteException e) {
			System.err.println("Erro ao obter Barrels do Gateway: " + e.getMessage());
		}
	}


	/**
     * Classe interna para tarefas de download e processa as páginas web
     */
    class DownloaderTask extends RecursiveTask<Void> {
        private final String url;

		/**
         * Construtor da DownloaderTask.
         * @param url URL a ser processada.
         */
        public DownloaderTask(String url) {
            this.url = url;
        }

        @Override
        protected Void compute() {
            try {
                if (visitedUrls.contains(url)) {
                    return null;
                }
                visitedUrls.add(url);
				Document doc = Jsoup.connect(url).get();
				String text = doc.text();
				String title = doc.title();
				String description = "";
				Element firstParagraph = doc.select("p").first();

				if(title.isEmpty()){
					title = "Sem título";
				}
				if (firstParagraph != null && !firstParagraph.text().isBlank()) {
					description = firstParagraph.text();
				} else {
					description = "Sem descrição"; 
				}
				sendToBarrelsInfo(url, title, description);

				String[] words = text.toLowerCase().split("\\W+");
				for (String word : words) {
					if (word.length() > 2 && !word.isEmpty()) {
						sendToBarrels(word, url);
					}
				}

				Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String newUrl = link.absUrl("href");
                    if (!newUrl.isEmpty() && !visitedUrls.contains(newUrl)) {
						sendLinkToBarrels(url, newUrl);
						putURL(newUrl);
                    }
                }

            } catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
            } catch (MalformedURLException e){
				System.out.println("Error: " + e.getMessage());
			} catch (HttpStatusException e){		
				System.out.println("HTTP error" + e.getMessage());
			}catch (Exception e){
				e.printStackTrace();
			}
            return null;
        }
    }


	/**
	 * Método para adicionar uma nova URL à lista de URLs 
	 * @param url URL a ser adicionada.
	 * @throws RemoteException
	 */
	public void putURL(String url) throws RemoteException{
		gateway.putNew(url);
	}


	/**
	 * Método para obter uma URL da lista de URLs
	 * @return URL a ser processada.
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	public String getURL() throws RemoteException, InterruptedException {
		String url = gateway.getNextURL();
		return url;
	}


	/**
	 * Método para atualizar a lista de Barrels
	*/
	private void updateBarrels() {
       try {
            List<BarrelInterface> newBarrels = gateway.getRegisteredBarrels();
            synchronized (this) {
                barrels.clear();
                barrels.addAll(newBarrels);
            }
            System.out.println("🔄 Downloader " + downloaderNumber + " atualizou Barrels. Ativos: " + newBarrels.size());
        } catch (RemoteException e) {
            System.err.println("Erro ao atualizar Barrels: " + e.getMessage());
        }
    }


	/**
     * Método para enviar uma palavra e o URL onde foi encontrada para os barrels
     * @param word Palavra extraída da página.
     * @param url URL onde a palavra foi encontrada.
     */
    private void sendToBarrels(String word, String url) {
		List<BarrelInterface> barrelsToRemove = new ArrayList<>();
        for (BarrelInterface barrel : barrels) {
            try {
                barrel.receiveWord(url, word);
            } catch (RemoteException e) {
                System.err.println("Error sending word to barrel: " + e.getMessage());
				barrelsToRemove.add(barrel);
            }
        }
		// Remove Barrels que falharam
        if (!barrelsToRemove.isEmpty()) {
            synchronized (this) {
                barrels.removeAll(barrelsToRemove);
            }
            System.out.println("Removidos " + barrelsToRemove.size() + " Barrels inativos");
        }
    }


	/**
     * Método para enviar a informaçãp da página (URL, título e descrição) para os barrels
     * @param url URL da página.
     * @param title Título da página.
     * @param description Descrição da página.
     */
	private void sendToBarrelsInfo(String url, String title, String description) {
		int count = 0;
        for (BarrelInterface barrel : barrels) {
            try {
				boolean send = barrel.receiveUrlInfo(url, title, description);
				//System.out.println("A mandar info para barrel");
				while(!send){
					send = barrel.receiveUrlInfo(url, title, description);
					count++;
					System.out.println("Unsucessfull attemps: " + count);
					if(count == totalAttemps){
						barrel.writeAllInformationToFile();
						Thread.sleep(1000);
						gateway.removeBarrel(barrel.getBarrelNumber());
						break;
					}
				}
            } catch (RemoteException e) {
                System.err.println("Error sending URL info to barrel: " + e.getMessage());
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

	/**
	 * Método para enviar um link que aponta para outro para os barrels
	 * @param sourceUrl URL de origem.
	 * @param targetUrl URL de destino.
	 */
	private void sendLinkToBarrels(String sourceUrl, String targetUrl) {
        for (BarrelInterface barrel : barrels) {
            try {
				//System.out.println("A mandar links to barrel");
                barrel.receiveLink(sourceUrl, targetUrl);
            } catch (RemoteException e) {
                System.err.println("Error sending link to barrel: " + e.getMessage());
            }
        }
    }


	/**
	 * Método main da classe
	 * @param args
	*/
    public static void main(String[] args) {
        try {
			Properties properties = new Properties();
			try {
				InputStream input = Downloader.class.getClassLoader().getResourceAsStream("config.properties");
				if (input == null) {
					System.err.println("Sorry, unable to find config.properties");
					return;
				}
				properties.load(input);
			} catch (IOException e) {
				System.out.println("Error loading config file");
				e.printStackTrace();
			}try{
					String host = properties.getProperty("gatewayIP"); // Endereço IP ou nome do host
				int port = Integer.parseInt(properties.getProperty("gatewayPort")); // Porta do RMI Registry
				
				GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry(host, port).lookup("Gateway");

				Downloader downloader = new Downloader(gateway);
					try {
						while (true) {
							String url = downloader.getURL(); 
							//System.out.println("Downloader Started 4");
							if(url == null){
								System.out.println("Waiting for URL...");
								Thread.sleep(1000);
							}else{
								System.out.println("A processar: " + url);
								downloader.new DownloaderTask(url).compute();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
			}catch(Exception e){
				e.printStackTrace();
			}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
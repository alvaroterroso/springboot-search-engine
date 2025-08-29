package googol.backend;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.*;


/**
 * Classe que representa um Storage Barrel, responsável 
 * por armazenar os dados da aplicação
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class Barrel extends UnicastRemoteObject implements BarrelInterface{

	/**
	 * Representa a conexão com o Gateway
	 */
	private GatewayInterface gateway;

	/**
	 * ID do storage barrel
	 */
	final int barrelNumber;

	/**
	 * Índice que associa uma palavra aos links em que ocorre 
	 */
	private final Map<String, HashSet<String>> index = new HashMap<>();

	/**
	 * Informação de cada url título e descrição 
	 */
	private final HashMap<String, SimpleEntry<String, String>> urlInfos = new HashMap<>();
	
	/**
	 * Links que apontam para um determinado URL.
	 */
	private final Map<String, HashSet<String>> reverseLinks = new HashMap<>();

	/**
	* Construtor da classe, recebe dados para a inicialização
	* dos atributos
	* @param gateway Interface do Gateway
	* @throws RemoteException se ocorrer um erro na comunicação remota
	*/
    public Barrel(GatewayInterface gateway) throws RemoteException{
		super();
		this.gateway = gateway;
		this.barrelNumber = gateway.getNextBarrelNumber();
		try {
			Properties properties = new Properties();
			InputStream input = Barrel.class.getClassLoader().getResourceAsStream("config.properties");
			if (input == null) {
				System.err.println("Sorry, unable to find config.properties");
				return;
			}
			properties.load(input);

			loadDataFromFile();
			
			gateway.registerBarrel(this);
			System.out.println("Barrel " + barrelNumber + " iniciado.");

        } catch (IOException e) {
            throw new RemoteException("Erro ao inicializar MulticastSocket", e);
        }
    }


	@Override
	public Map<String, HashSet<String>> getIndex() throws RemoteException {
        return index;
    }

	@Override
	public int getIndexSize() throws RemoteException {
		synchronized (index) {
			return index.size();
		}
	}

	@Override
	public int getLinkCount(String url) throws RemoteException {
		synchronized (reverseLinks) {
			HashSet<String> links = reverseLinks.get(url);
			return (links != null) ? links.size() : 0;  
		}
	}

	@Override
	public int getBarrelNumber() throws RemoteException {
		return barrelNumber;
	}


	@Override
    public void addLink(String sourceUrl, String targetUrl) throws RemoteException {
		synchronized (reverseLinks) {
			HashSet<String> sources = reverseLinks.get(targetUrl);
			if (sources == null) {
				sources = new HashSet<>();
				reverseLinks.put(targetUrl, sources);
			}
			sources.add(sourceUrl);
			
		}
    }

	

	@Override
	public List<String> searchMultipleWords(String[] words) throws RemoteException {
		synchronized (index) {
			if (words == null || words.length == 0) {
				return new ArrayList<>();
			}
	
			Set<String> result = null;
	
			for (String word : words) {
				//System.out.println("Palavra '" + word + "' dentro do for");
				word = word.toLowerCase();
				//System.out.println(index);
				HashSet<String> urlsForWord = index.get(word);
	
				if (urlsForWord == null) {
					//System.out.println("urlsForWord == null");
					return new ArrayList<>();
				}
	
				if (result == null) {
					result = new HashSet<>(urlsForWord);
				} else {
					result.retainAll(urlsForWord);
				}
			}
			List<String> resultTitles = new ArrayList<>();
			if (result != null) {
				for (String url : result) {
					SimpleEntry<String, String> urlInfo = urlInfos.get(url);
					if (urlInfo != null) {
						String title = urlInfo.getKey();
						String description = urlInfo.getValue();
						resultTitles.add(url + " | Título: " + title + " | Descrição: " + description );
					}
				}
			}


        	return resultTitles;
		}
	}


	@Override
	public List<String> getPagesLinkingToPage(String url) throws RemoteException {
		synchronized (reverseLinks) {
			HashSet<String> sources = reverseLinks.get(url);
			if (sources != null) {
				return new ArrayList<>(sources);
			} else {
				return new ArrayList<>();
			}
		}
	}

	@Override
    public LinkedHashMap<String, Integer> getTop10PagesByLinks() throws RemoteException {
        synchronized (reverseLinks) {
            Map<String, Integer> linkCountMap = new HashMap<>();
            
            for (Map.Entry<String, HashSet<String>> entry : reverseLinks.entrySet()) {
                linkCountMap.put(entry.getKey(), entry.getValue().size());
            }
            
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(linkCountMap.entrySet());
            entries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
            
            LinkedHashMap<String, Integer> top10Pages = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(10, entries.size()); i++) {
                Map.Entry<String, Integer> entry = entries.get(i);
                String pageUrl = entry.getKey();
                int referenceCount = entry.getValue();

                //System.out.println("Página: " + pageUrl + " | Referências: " + referenceCount);
                top10Pages.put(pageUrl, referenceCount);
            }
            return top10Pages;
        }
    }

	/**
	 * Método para armazenar uma palavra e o URL onde foi encontrada
	 * @param url URL onde a palavra foi encontrada
	 * @param word Palavra a ser armazenada
	 * 
	 * */
    protected synchronized void storeWordAndUrl(String url,String word) {
		synchronized(index){
			word = word.toLowerCase();  

			// Verifica se a palavra já existe no índice
			if (index.containsKey(word)) {
				HashSet<String> urls = index.get(word);
				if (urls != null) {
					urls.add(url);
				} else {
					urls = new HashSet<>();
					urls.add(url);
					index.put(word, urls);	
				}
			} else {
				HashSet<String> urls = new HashSet<>();
				urls.add(url);
				index.put(word, urls);
			}

		}
	}


	@Override
    public synchronized boolean receiveWord(String url, String word) throws RemoteException {
        storeWordAndUrl(url, word.toLowerCase());
		return true;
    }

	@Override
    public synchronized boolean receiveUrlInfo(String url, String title, String description) throws RemoteException {
        urlInfos.put(url, new SimpleEntry<>(title, description));
		return true;
    }

	@Override
    public synchronized boolean receiveLink(String sourceUrl, String targetUrl) throws RemoteException {
        addLink(sourceUrl, targetUrl);
		return true;
    }

	/**
	 * Método para carregar os dados do ficheiro do barrel
	 */
	private synchronized void loadDataFromFile() {
		String fileName = "barrel" + barrelNumber + ".txt";
		File file = new File(fileName);
		
		if (!file.exists()) {
			System.out.println("Ficheiro " + fileName + " não encontrado. Iniciando com dados vazios.");
			return;
		}
	
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("word|")) {
					processWordLine(line);
				} else if (line.startsWith("info|")) {
					processInfoLine(line);
				} else if (line.startsWith("connections|")) {
					processConnectionsLine(line);
				}
			}
			System.out.println("Dados carregados do ficheiro " + fileName + 
							 ": " + index.size() + " palavras, " + 
							 urlInfos.size() + " URLs, " + 
							 reverseLinks.size() + " conexões");
		} catch (IOException e) {
			System.err.println("Erro ao ler o ficheiro " + fileName + ": " + e.getMessage());
		}
	}

	/**
	 * Método para processar uma linha de palavras
	 * @param line Linha a ser processada
	 */
	private void processWordLine(String line) {
		// Formato: word|palavra: url1, url2, url3
		String[] parts = line.split("\\|");
		if (parts.length < 2) return;
		
		String[] wordAndUrls = parts[1].split(";");
		if (wordAndUrls.length < 2) return;
		
		String word = wordAndUrls[0].trim();
		String[] urls = wordAndUrls[1].split(",");
		
		HashSet<String> urlSet = new HashSet<>();
		for (String url : urls) {
			urlSet.add(url.trim());
		}
		
		index.put(word, urlSet);
	}

	/**
	 * Método para processar uma linha de informações
	 * @param line Linha a ser processada
	 */
	private void processInfoLine(String line) {
		// Formato: info|url|título| descrição
		String[] parts = line.split("\\|");
		if (parts.length < 4) return;
		
		String url = parts[1].trim();
		String title = parts[2].trim();
		String description = parts[3].trim();
		
		urlInfos.put(url, new SimpleEntry<>(title, description));
	}

	/**
	 * Método para processar uma linha de conexões
	 * @param line Linha a ser processada
	 */
	private void processConnectionsLine(String line) {
		// Formato: connections|url: url1, url2, url3
		String[] parts = line.split("\\|");
		if (parts.length < 2) return;
		
		String[] urlAndSources = parts[1].split(";");
		if (urlAndSources.length < 2) return;
		
		String url = urlAndSources[0].trim();
		String[] sources = urlAndSources[1].split(",");
		
		HashSet<String> sourceSet = new HashSet<>();
		for (String source : sources) {
			sourceSet.add(source.trim());
		}
		
		reverseLinks.put(url, sourceSet);
	}


	@Override
	public synchronized void writeAllInformationToFile() throws RemoteException {
		String fileName = "barrel" + barrelNumber + ".txt"; 
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) { 

			// Escreve as palavras no índice
			for (Map.Entry<String, HashSet<String>> entry : index.entrySet()) {
				String word = entry.getKey();
				HashSet<String> urls = entry.getValue();
				String line = "word|" + word + "; " + String.join(", ", urls);
				writer.write(line);
				writer.newLine();
			}

			// Escreve as informações sobre os URLs (título e descrição)
			for (Map.Entry<String, SimpleEntry<String, String>> entry : urlInfos.entrySet()) {
				String url = entry.getKey();
				SimpleEntry<String, String> info = entry.getValue();
				String line = "info|" + url + "|" + info.getKey() + "| " + info.getValue();
				writer.write(line);
				writer.newLine();
			}

			// Escreve os links reversos
			for (Map.Entry<String, HashSet<String>> entry : reverseLinks.entrySet()) {
				String targetUrl = entry.getKey();
				HashSet<String> sources = entry.getValue();
				String line = "connections|" + targetUrl + "; " + String.join(", ", sources);
				writer.write(line);
				writer.newLine();

			}
			writer.flush(); 

		} catch (IOException e) {
			System.err.println("Erro ao gravar no arquivo: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	/**
	 * Método main da classe
	 */
    public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			// Substitua esta linha:
			// properties.load(new FileInputStream("config.properties"));
			// Por esta:
			InputStream input = Barrel.class.getClassLoader().getResourceAsStream("config.properties");
			if (input == null) {
				System.err.println("Sorry, unable to find config.properties");
				return;
			}
			properties.load(input);
	
			String host = properties.getProperty("gatewayIP");
			int port = Integer.parseInt(properties.getProperty("gatewayPort"));
	
			Registry registry = LocateRegistry.getRegistry(host, port);
			GatewayInterface gateway = (GatewayInterface) registry.lookup("Gateway");
			
			Barrel barrel = new Barrel(gateway);
	
			final Barrel finalBarrel = barrel;
	
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n[LOG] Barrel " + finalBarrel.barrelNumber + " recebeu Ctrl+C. A guardar no ficheiro...");
				try {
					finalBarrel.writeAllInformationToFile();
					Thread.sleep(1000);
					gateway.removeBarrel(finalBarrel.barrelNumber);
					System.out.println("Barrel " + finalBarrel.barrelNumber + " removido com sucesso do Gateway.");
				} catch (Exception e) {
					System.err.println("Erro ao escrever informações no arquivo durante o shutdown: " + e.getMessage());
				}
			}));
			
			while (true) {
				Thread.sleep(1000);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
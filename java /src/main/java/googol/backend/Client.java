package googol.backend;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.*;
import java.io.*;
import org.jsoup.*;

/**
 * Classe que representa o cliente, por onde o utilizador interage com o sistema
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class Client {
	
	/**
	 * Método principal
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Client Started");
	
			Properties properties = new Properties();
			try {
				// Carrega o arquivo do classpath
				InputStream input = Client.class.getClassLoader().getResourceAsStream("config.properties");
				if (input == null) {
					// Fallback 1: Tenta carregar do diretório atual
					try {
						input = new FileInputStream("config.properties");
					} catch (FileNotFoundException e) {
						// Fallback 2: Tenta caminho absoluto
						input = new FileInputStream("/mnt/c/UC/Sistemas Distribuídos/SD/projetoSD/java/target/classes/config.properties");
					}
				}
				
				if (input == null) {
					System.err.println("Error: config.properties not found in any location");
					return;
				}
				
				properties.load(input);
				input.close();
	
			} catch (IOException e) {
				System.out.println("Error loading config file");
				e.printStackTrace();
				return;
			}
	
			String host = properties.getProperty("gatewayIP");
			int port = Integer.parseInt(properties.getProperty("gatewayPort"));

			GatewayInterface gateway = (GatewayInterface) LocateRegistry.getRegistry(host, port).lookup("Gateway");

			try{
				Scanner scanner = new Scanner(System.in);
				while (true) {
                System.out.println("1. Adicionar URL para indexação");
                System.out.println("2. Pesquisar uma palavra");
				System.out.println("3. Pesquisar links que apontam para uma dada página");
				System.out.println("4. Top 10 com mais ligações");
				System.out.println("5. Estatísticas");
                System.out.println("6. Sair");
                System.out.print("Escolha uma opção: ");
                int option;
				if (scanner.hasNextInt()) {
					option = scanner.nextInt();
					scanner.nextLine();
				} else {
					System.out.println("Opção inválida. Tente novamente.");
					scanner.nextLine();
					continue;
				}
                switch (option) {
                    case 1:
                        System.out.print("URL a indexar: ");
                        String url = scanner.nextLine().trim();
						if (!(url.startsWith("http://") || url.startsWith("https://"))) {
							System.out.println("URL inválida.");
							break;
						}
						try {
							int statusCode = Jsoup.connect(url).ignoreHttpErrors(true).execute().statusCode();
							if (statusCode >= 400) {
								System.out.println("Erro a adicionar o URL (HTTP " + statusCode + ")");
								break;
							}
						} catch (IOException e) {
							System.out.println("Erro ao conectar à URL: " + url);
							break;
						}
                        gateway.putNew(url); 
                        break;
					case 2:    
						System.out.print("Palavras para pesquisar (separadas por espaço): ");
						String input = scanner.nextLine();
						
						int currentPage = 1;
						boolean continueSearching = true;
						
						while (continueSearching) {
							try {
								List<String> urls = gateway.search(input, currentPage,false);
								
								if (!urls.isEmpty()) {
									System.out.println("\nPágina " + currentPage + " - URLs encontradas para '" + input + "':");
									
									for (int i = 0; i < urls.size(); i++) {
										System.out.println((i + 1) + ". " + urls.get(i));
									}
									
									System.out.println("\nOpções:");
									if (currentPage > 1) {
										System.out.println("(P) Página anterior");
									}
									if (urls.size() == 10) {
										System.out.println("(N) Próxima página");
									}
									System.out.println("(S) Sair da pesquisa");
									
									System.out.print("Escolha uma opção: ");
									String userInput = scanner.nextLine().trim().toLowerCase();
									
									if (userInput.equals("n") && urls.size() == 10) {
										currentPage++;
									} else if (userInput.equals("p") && currentPage > 1) {
										currentPage--;
									} else if (userInput.equals("s")) {
										continueSearching = false;
									} else {
										System.out.println("Opção inválida. Voltando ao menu principal.");
										continueSearching = false;
									}
								} else {
									if (currentPage == 1) {
										System.out.println("Nenhuma URL encontrada para as palavras '" + input + "'.");
									} else {
										System.out.println("Não há mais resultados para mostrar.");
									}
									continueSearching = false;
								}
							} catch (RemoteException e) {
								System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
								continueSearching = false;
							}
						}
						break;
					case 3:
						System.out.print("Página a pesquisar: ");
						String searchUrl = scanner.nextLine();
						List<String> linkingPages = gateway.getPagesLinkingTo(searchUrl);
						if (!linkingPages.isEmpty()) {
							System.out.println("Páginas que apontam para '" + searchUrl + "':");
							for (String page : linkingPages) {
								System.out.println(page);
							}
						} else {
							System.out.println("Nenhuma página aponta para '" + searchUrl+ "'.");
						}
						break;
					case 4:
						LinkedHashMap<String, Integer> top10Pages = gateway.getTop10PagesByLinks();
						if (top10Pages.isEmpty()) {
							System.out.println("Nenhuma página encontrada.");
						} 
						else {
							int i = 1;
							for (Map.Entry<String, Integer> entry : top10Pages.entrySet()) {
								System.out.println(i + " - " + entry.getKey() + " | Referências: " + entry.getValue());
								i++;
							}
						}
						break;
					case 5:
						LinkedHashMap<String, Integer> mostSearchedWords = gateway.getMostSearchedWords();
						List<Integer> indexSizes = gateway.getBarrelsIndexSizes();
						if (mostSearchedWords.isEmpty()) {
							System.out.println("Nenhuma pesquisa registada.");
							
						} else {
							int i = 1;
							for (Map.Entry<String, Integer> entry : mostSearchedWords.entrySet()) {
								System.out.println(i + " - " + entry.getKey() + " | Pesquisas: " + entry.getValue());
								i++;
							}
							System.out.println("Tempo médio de resposta: "+ gateway.getAverageResponseTime() + " décimas de segundo");

						}
						System.out.println("Tamanho dos índices por Barrel:");
						for (int i = 0; i < indexSizes.size(); i++) {
							System.out.println("Barrel " + (i + 1) + ": " + indexSizes.get(i) + " palavras indexadas");
						}
						String statistics = gateway.getFormattedStatistics();
						System.out.println(statistics);
						
						break;
					case 6:
						System.out.println("A sair");
						scanner.close();
						System.exit(0);
						break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }
			}catch(Exception e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	 }
}
package googol.backend;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;
import java.io.*;


/**
 * Classe que representa Gateway do sistema
 *  @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */

public class Gateway extends UnicastRemoteObject implements GatewayInterface {
    
	/**
	 * Representa a conexão com URLQueue
	 */
	private volatile URLQueueInterface urlQueue;

	/**
	 * Representa a conexão com os Barrels
	 */
	private volatile ArrayList<BarrelInterface> barrels;
	
	/**
	* Lista de pesquisas e respetivo número de ocorrências
	 */
	private final Map<String, Integer> searchHistory = new HashMap<>();

	/**
	 * Lista de tempos de resposta das requisições
	 */
	public ArrayList<Float> responseTimes = new ArrayList<>();

	/**
	 * Número de Barrels
	 */
	private int barrelCounter = 0;

	/**
	 * Número de downloaders ativos
	 */
	private int activeDownloaders = 0;

	/**
	 * Construtor da classe Gateway
	 * @throws RemoteException
	 */
    public Gateway() throws RemoteException {
        super();
       	urlQueue =  null;
		barrels = new ArrayList<BarrelInterface>();

    }

	@Override
	public void initQueue(URLQueueInterface queue) throws RemoteException {	
		this.urlQueue = queue;
	}
   
	@Override
    public void putNew(String url) throws RemoteException {
        urlQueue.addURL(url);
    }

	@Override
	public String getNextURL() throws RemoteException, InterruptedException {
		if (urlQueue == null) {
			throw new RemoteException("URLQueue is not initialized.");
		}
		return urlQueue.getURL(); 
	}

	@Override
	public List<String> search(String input, int page, boolean isPagination) throws RemoteException {
		long startTime = System.nanoTime();
		if (!isPagination) {
			registerSearch(input);
		}

		String[] words = input.split(" ");
		List<String> results = new ArrayList<>();
		for (BarrelInterface barrel : barrels) {
			try {
				List<String> barrelResults = barrel.searchMultipleWords(words);
				if (results.isEmpty()) {
					results.addAll(barrelResults);
				} else {
					results.retainAll(barrelResults);
				}
			} catch (RemoteException error) {
				System.out.println(error);
			}
		}
		
		// Sort results by link count in descending order
		results.sort(Comparator.comparingInt((String urlString) -> {
			String url = urlString.split(" \\| ")[0];
			try {
				return getLinkCount(url);
			} catch (RemoteException e) {
				System.err.println("Error while getting link count for " + url + ": " + e.getMessage());
				return 0;
			}
		}).reversed());

		// Implement pagination with a new ArrayList to ensure serialization
		int pageSize = 10;
		int startIndex = (page - 1) * pageSize;
		int endIndex = Math.min(startIndex + pageSize, results.size());
		
		List<String> paginatedResults = new ArrayList<>();
		if (startIndex < results.size()) {
			paginatedResults = new ArrayList<>(results.subList(startIndex, endIndex));
		}

		long endTime = System.nanoTime();
		float responseTime = (endTime - startTime) / 100_000_000.0f;
		responseTimes.add(responseTime);
		
		return paginatedResults;
	}

	public int getLinkCount(String url) throws RemoteException {
		int linkCount = 0;
		for (BarrelInterface barrel : barrels) {
			try {
				int count = barrel.getLinkCount(url);
				linkCount += count;
			} catch (RemoteException e) {
				System.err.println("Error while getting link count from Barrel: " + e.getMessage());
			}
		}
		return linkCount;
	}

	@Override
	public List<String> getPagesLinkingTo(String url) throws RemoteException {
		List<String> result = new ArrayList<>();

		for (BarrelInterface barrel : barrels) {
			try {
				List<String> barrelResults = barrel.getPagesLinkingToPage(url);
				result.addAll(barrelResults);
			} catch (RemoteException e) {
				System.err.println("Erro ao consultar o Barrel: " + e.getMessage());
			}
		}

		return new ArrayList<>(new HashSet<>(result));
	}

	@Override
	public  LinkedHashMap<String, Integer> getTop10PagesByLinks() throws RemoteException {
		Map<String, Integer> combinedLinkCounts = new HashMap<>();

		for (BarrelInterface barrel : barrels) {
			try {
				LinkedHashMap<String, Integer> barrelTop10 = barrel.getTop10PagesByLinks();

				for (Map.Entry<String, Integer> entry : barrelTop10.entrySet()) {
					String page = entry.getKey();
					int count = entry.getValue();
					combinedLinkCounts.put(page, combinedLinkCounts.getOrDefault(page, 0) + count);
				}
			} catch (RemoteException e) {
				System.err.println("Erro ao consultar o Barrel: " + e.getMessage());
			}
		}

		// Ordena 
		LinkedHashMap<String, Integer> top10Pages = new LinkedHashMap<>();
		List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(combinedLinkCounts.entrySet());
		sortedEntries.sort((entry1, entry2) -> entry2.getValue() - entry1.getValue());

		for (int i = 0; i < Math.min(10, sortedEntries.size()); i++) {
			top10Pages.put(sortedEntries.get(i).getKey(), sortedEntries.get(i).getValue());
		}
		return top10Pages;
	}

	@Override
	public LinkedHashMap<String, Integer> getMostSearchedWords() throws RemoteException {
		List<Map.Entry<String, Integer>> entries = new ArrayList<>(searchHistory.entrySet());

		entries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		for (Map.Entry<String, Integer> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}


	@Override
	public void registerBarrel(BarrelInterface barrel) throws RemoteException {
		barrels.add(barrel);
		System.out.println("Barrel registrado com sucesso.");
	}

	@Override
	public List<Integer> getBarrelsIndexSizes() throws RemoteException {
		List<Integer> indexSizes = new ArrayList<>();
		for (BarrelInterface barrel : barrels) {
			try {
				int size = barrel.getIndexSize();
				indexSizes.add(size);
			} catch (RemoteException e) {
				System.err.println("Erro ao obter o tamanho do índice do Barrel: " + e.getMessage());
				indexSizes.add(-1); // Adiciona -1 em caso de erro
			}
		}
		return indexSizes;
	}

	@Override
	public float getAverageResponseTime() throws RemoteException {
		if (responseTimes.size() == 0) {
            return 0;
        }
		float sum = 0;
		for (Float time : responseTimes) {
			sum += time;
		}
		return sum / responseTimes.size();
	}

	@Override
	public List<BarrelInterface> getRegisteredBarrels() throws RemoteException {
        List<BarrelInterface> activeBarrels = new ArrayList<>();
        for (BarrelInterface barrel : barrels) {
            try {
                barrel.getIndexSize();
                activeBarrels.add(barrel);

            } catch (RemoteException e) {
                System.err.println("Barrel inativo removido: " + e.getMessage());
            }
        }
        barrels.clear();
        barrels.addAll(activeBarrels);
        return new ArrayList<>(barrels); 
	}

	@Override
	public synchronized int getNextBarrelNumber() throws RemoteException {
    	return ++barrelCounter;
	}

	@Override
	public synchronized int registerDownloader() throws RemoteException {
		return ++activeDownloaders;
	}

	@Override
	public void removeBarrel(int id) throws RemoteException {
		barrelCounter--;
		for (BarrelInterface barrel : barrels) {
			if (barrel.getBarrelNumber() == id) {
				barrels.remove(barrel);
				break;
			}
		}
		getFormattedStatistics();
		System.out.println("Barrel " +  id + " removido");
	
	}

	/**
	 * Método para registar uma pesquisa
	 * @param input pesquisa a registar
	 */
	public void registerSearch(String input) {
        searchHistory.put(input, searchHistory.getOrDefault(input, 0) + 1);
    }

		/**
	 * Retorna todas as estatísticas consolidadas numa única String formatada.
	 * @return String com as estatísticas formatadas
	 * @throws RemoteException
	 */
	
	@Override
	public String getFormattedStatistics() throws RemoteException {
		StringBuilder stats = new StringBuilder();
		LinkedHashMap<String, Integer> mostSearchedWords = getMostSearchedWords();
		List<Integer> indexSizes = getBarrelsIndexSizes();
		float avgResponseTime = getAverageResponseTime();

		// Palavras mais pesquisadas
		if (mostSearchedWords.isEmpty()) {
			stats.append("Nenhuma pesquisa registada.\n");
		} else {
			stats.append("Top 10 palavras mais pesquisadas:\n");
			int i = 1;
			for (Map.Entry<String, Integer> entry : mostSearchedWords.entrySet()) {
				if (i > 10) break;
				stats.append(i).append(" - ")
					.append(entry.getKey())
					.append(" | Pesquisas: ")
					.append(entry.getValue())
					.append("\n");
				i++;
			}
			stats.append("Tempo médio de resposta: ")
				.append(String.format("%.2f", avgResponseTime))
				.append(" décimas de segundo\n");
		}

		// Tamanho dos índices por Barrel
		stats.append("\nTamanho dos índices por Barrel:\n");
		for (int j = 0; j < indexSizes.size(); j++) {
			stats.append("Barrel ").append(j + 1)
				.append(": ").append(indexSizes.get(j))
				.append(" palavras indexadas\n");
		}

		return stats.toString();
	}

	/**
	 * Método main
	 * @param args
	 */
	public static void main(String args[]) {
        try {
            Gateway gateway = new Gateway();

			Properties properties = new Properties();
			try {
            	InputStream input = Gateway.class.getClassLoader().getResourceAsStream("config.properties");
				if (input == null) {
					System.err.println("Sorry, unable to find config.properties");
					return;
				}
				properties.load(input);
			} catch (IOException e) {
				System.out.println("Error loading config file");
				e.printStackTrace();
			}

			String host = properties.getProperty("gatewayIP"); // Endereço IP ou nome do host
			int port = Integer.parseInt(properties.getProperty("gatewayPort")); // Porta do RMI Registry
			
			//RMI connection
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("Gateway", gateway);

			URLQueueInterface urlQueue = new URLQueue(gateway);
        	gateway.initQueue(urlQueue);

			System.out.println("RMI connected, Gateway Started");


        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
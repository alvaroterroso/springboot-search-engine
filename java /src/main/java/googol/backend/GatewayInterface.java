package googol.backend;

import java.rmi.*;
import java.util.*;

/**
 * Interface que fornece métodos para obter informações do Gateway remotamente através de RMI
 */
public interface GatewayInterface extends Remote{

	/**
	 * Método para inicializar a fila de URLs
	 * @throws RemoteException exceção 
	 */
	void initQueue(URLQueueInterface queue) throws RemoteException;

	/**
	 * Método para adicionar um link para ser indexado
	 * @param url link a ser indexado
	 * @throws RemoteException exceção 
	 */
	void putNew(String url) throws RemoteException;

	/**
	 * Método para obter um link para ser indexado
	 * @return link a ser indexado
	 * @throws RemoteException exceção 
	 * @throws InterruptedException exceção 
	 */
	String getNextURL() throws RemoteException, InterruptedException;

	/**
	 * Método para registar um barrel
	 * @param barrel barrel a registar
	 */
	void registerBarrel(BarrelInterface barrel) throws RemoteException;


	/**
	 * Método para pesquisar um input no índice 
	 * @param input palavra a pesquisar
	 * @return lista de urls que contêm a palavra
	 * @throws RemoteException exceção 
	 */
	List<String> search(String input, int page, boolean isPagination) throws RemoteException;


	/**
	 * Método para obter o número de links que apontam para uma dada página
	 * @param url url
	 * @return número de links que apontam para a página
	 * @throws RemoteException exceção 
	 */
	int getLinkCount(String url) throws RemoteException; 

	/**
	 * Método que devolve as 10 páginas com mais links
	 * @return lista com as 10 páginas com mais links
	 * @throws RemoteException exceção 
	 */
	LinkedHashMap<String, Integer> getTop10PagesByLinks() throws RemoteException;

	/**
	 * Método para pesquisar um conjunto de palavras no índice 
	 * @return lista de urls que comuns todas as palavras
	 * @throws RemoteException exceção 
	 */
	LinkedHashMap<String, Integer> getMostSearchedWords() throws RemoteException;

	/**
	 * Método que devolve a lista de links que apontam para uma dada página
	 * @param url url 
	 * @return lista de links que apontam para a página
	 * @throws RemoteException exceção 
	 */
	List<String> getPagesLinkingTo(String url) throws RemoteException;

	/**
	 * Método para remover um barrel
	 * @param id id do barrel
	 * @throws RemoteException exceção 
	 */
	void removeBarrel(int id) throws RemoteException;


	/**
	 * Método para obter o tamanho do índice de cada barrel
	 * @return lista com o tamanho do índice de cada barrel
	 * @throws RemoteException exceção 			
	 */
	List<Integer> getBarrelsIndexSizes() throws RemoteException;


	/**
	 * Método para calcular o tempo médio de resposta 
	 * @return tempo médio de resposta
	 */
	float getAverageResponseTime() throws RemoteException;

	/**
	 * Método para obter a lista de barrels registados
	 * @return lista de barrels registados
	 * @throws RemoteException exceção 
	 */
	List<BarrelInterface> getRegisteredBarrels() throws RemoteException;

	/**
	 * Método para obter o número do próximo barrel
	 * @return número do próximo barrel
	 * @throws RemoteException exceção 
	 */
	int getNextBarrelNumber() throws RemoteException;

	/**
	 * Método para registar um downloader
	 * @return número de downloaders registados
	 * @throws RemoteException exceção 
	 */
	int registerDownloader() throws RemoteException;

	String getFormattedStatistics() throws RemoteException;
	

}

package googol.backend;

import java.rmi.*;
import java.util.*;


/**
 * Interface fornece métodos para obter informações dos storage barrels remotamente através de RMI
 */
public interface BarrelInterface extends Remote {

	/**
	 * Método que devolve o índice do barrel, índice que associa uma palavra aos links em que ocorre  
	 * @return índice do barrel
	 */
	Map<String, HashSet<String>> getIndex() throws RemoteException;

	/**
	 * Método que devolve o número de links que um dado link aponta
	 * @return número de links que o link aponta
	 */
	int getIndexSize() throws RemoteException;

	/**
	 * Método para devolver o número do barrel
	 * @return número do barrel
	 */
	int getBarrelNumber() throws RemoteException;

	/**
	 * Método para adicionar um link que aponta para outro 
	 * @param sourceUrl url em questão 
	 * @param targetUrl target
	 */
	void addLink(String sourceUrl, String targetUrl) throws RemoteException;

	/**
	 * Método para pesquisar um conjunto de palavras no índice 
	 * @param words conjunto de palavras a pesquisar
	 * @return lista de urls que comuns todas as palavras
	 */
	List<String> searchMultipleWords(String[] words) throws RemoteException;

	/**
	 * Método que devolve a lista de links que apontam para uma dada página
	 * @param url url
	 * @return lista de links que apontam para a página
	 * @throws RemoteException
	 */
	List<String> getPagesLinkingToPage(String url) throws RemoteException;

	/**
	 * Método que devolve as 10 páginas com mais links
	 * @return lista com as 10 páginas com mais links
	 */
	LinkedHashMap<String, Integer> getTop10PagesByLinks() throws RemoteException;

	/**
	 * Método que devolve o número de links que apontam para uma dada página
	 * @param url url
	 * @return número de links que apontam para a página
	 */
	int getLinkCount(String url) throws RemoteException;

	
	/**
	 * Método para receber do downloader uma palavra e os links em que ocorre 
	 * @param url,word url e word
	 * @return true se a palavra foi recebida com sucesso
	 */
	boolean receiveWord(String url, String word) throws RemoteException;

	/**
	 * Método para receber informação de um link
	 * @param url, title, description
	 * @return true se a informação foi recebida com sucesso
	 */
	boolean receiveUrlInfo(String url, String title, String description) throws RemoteException;

	/**
	 * Método para receber um link que aponta para outro 
	 * @param sourceUrl,targetUrl source e target
	 * @return true se os links foi recebido com sucesso
	 */
	boolean receiveLink(String sourceUrl, String targetUrl) throws RemoteException;

	/**
	 * Método para escrever toda a informação do barrel num ficheiro
	 */
	void writeAllInformationToFile() throws RemoteException;


}
package googol.backend;

import java.rmi.*;
import java.util.Map;

/**
 * Interface fornece métodos para obter informações dos downloaders remotamente através de RMI.
 */
public interface DownloaderInterface extends Remote {
	
	/**
	 * Método para adicionar um link para ser indexado
	 * @param url link a ser indexado
	 * @throws RemoteException 
	 */
	void putURL(String url) throws RemoteException;

	/**
	 * Método para obter um link para ser indexado
	 * @return link a ser indexado
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	String getURL() throws RemoteException, InterruptedException;

	/**
	 * Método para obter as estatísticas
	 * @return número de links que faltam ser indexados
	 * @throws RemoteException
	 */
	Map<String, Integer> getMessageStatistics() throws RemoteException;

}
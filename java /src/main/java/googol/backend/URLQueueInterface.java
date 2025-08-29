package googol.backend;

import java.rmi.*;
import java.util.concurrent.BlockingQueue;

/**
 * Interface que fornece métodos para obter informações da fila de URLs remotamente através de RMI.
 */
public interface URLQueueInterface {

	/**
	 * Metodo para adicionar um  URL à fila
	 * @param url
	 * @throws RemoteException
	 */
	void addURL(String url) throws RemoteException;

	/**
	 * Método para obter um URL da fila
	 * @return url
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	String getURL() throws RemoteException, InterruptedException;

	/**
	 * Método para verificar se a fila está vazia
	 * @return true se a fila estiver vazia, false caso contrário
	 * @throws RemoteException
	 */
	boolean isEmpty() throws RemoteException;

	/**
	 * Método para obter a fila de URLs
	 * @return fila de URLs
	 * @throws RemoteException
	 */
	BlockingQueue<String> getQueue() throws RemoteException;
	
}

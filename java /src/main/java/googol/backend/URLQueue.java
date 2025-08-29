package googol.backend;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;


/**
 * Classe URLQueue, responsável por armazenar os URLs 
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class URLQueue extends UnicastRemoteObject implements URLQueueInterface{
	
	/**
	 * Fila de URLs
	 */
    private BlockingQueue<String> queue;

	/**
	 * Representa a conexão com o Gateway
	 */
	private GatewayInterface gateway;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * @param gateway Interface do Gateway
	 * @throws RemoteException
	 */
    public URLQueue(GatewayInterface gateway) throws RemoteException {
        super();
        this.queue = new LinkedBlockingQueue<>();
		this.gateway = gateway;
    }

	@Override
    public synchronized void addURL(String url) throws RemoteException {
        queue.add(url);
        System.out.println("URL adicionada: " + url);
        notify();  
    }

	@Override
    public synchronized String getURL() throws RemoteException, InterruptedException {
        while (queue.isEmpty()) {
            System.out.println("🔁 Esperando por uma URL...");
            wait();  
        }
        return queue.take();
    }

    public synchronized BlockingQueue<String> getQueue() throws RemoteException {
        return queue;
    }

    public synchronized boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }

	/**
	 * Método main
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("config.properties"));
			String host = properties.getProperty("gatewayIP"); // Endereço IP ou nome do host
			int port = Integer.parseInt(properties.getProperty("gatewayPort")); // Porta do RMI Registry

			// Obtém o registro RMI
			Registry registry = LocateRegistry.getRegistry(host, port);

			// Procura o objeto "Gateway" no RMI Registry
			GatewayInterface gateway = (GatewayInterface) registry.lookup("Gateway");

			URLQueue queue = new URLQueue(gateway);

			

		} catch (Exception e) {
			System.err.println("Erro inesperado: " + e.getMessage());
			e.printStackTrace();
		}
	}
}


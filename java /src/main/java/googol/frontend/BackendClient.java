package googol.frontend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jakarta.annotation.PostConstruct;
import googol.backend.GatewayInterface;

/**
 * Classe responsável por estabelecer a conexão com o servidor RMI.
 * Esta classe é responsável por inicializar o cliente RMI e fornecer acesso ao GatewayInterface.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */

@Service
public class BackendClient {
    @Value("${gatewayIP}")
    private String gatewayIp;
    
    @Value("${gatewayPort}")
    private int gatewayPort;

    private GatewayInterface gateway;

	/**
	 * Método que inicializa o cliente RMI, estabelecendo a conexão com o servidor.
	 */
    @PostConstruct
    public void init() {
        try {
            System.out.println("Connecting to RMI server at {" + gatewayIp +"}:{" + gatewayPort + "}");
            Registry registry = LocateRegistry.getRegistry(gatewayIp, gatewayPort);
            this.gateway = (GatewayInterface) registry.lookup("Gateway");
            System.out.println("Successfully connected to RMI server");
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Failed to initialize RMI client", e);
        }
    }

	/**
	 * Método que retorna a instância do GatewayInterface.
	 * @return A instância do GatewayInterface.
	 */

    public GatewayInterface getGateway() {
        return gateway;
    }
}
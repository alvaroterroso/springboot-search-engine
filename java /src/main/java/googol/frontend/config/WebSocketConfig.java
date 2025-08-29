package googol.frontend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


/**Classe de configuração do WebSocket.
 * Esta classe configura o WebSocket para permitir a comunicação em tempo real entre o cliente e o servidor.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	/**
	 * Método para configurar o broker de mensagens e os endpoints do WebSocket.
	 * @param config Configuração do broker de mensagens.
	 */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Broker para os tópicos
        config.setApplicationDestinationPrefixes("/app"); // Prefixo para mensagens do cliente para o servidor
    }

	/**
	 * Método para registrar os endpoints do WebSocket.
	 * @param registry Registro dos endpoints do WebSocket.
	 * */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
		.addEndpoint("/stats-websocket")
		.setAllowedOrigins("https://localhost:8080") // Permitir apenas localhost para desenvolvimento
		.withSockJS(); // Endpoint WebSocket com fallback SockJS
		
    }
}

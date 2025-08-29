package googol.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Classe de configuração da aplicação.
 * Esta classe configura o RestTemplate para permitir chamadas HTTP para o backend.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
*/
@Configuration
public class AppConfig {
	/**
	 * Método para criar um bean RestTemplate.
	 * @return Um objeto RestTemplate configurado.
	 */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
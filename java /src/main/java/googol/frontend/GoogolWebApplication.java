package googol.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner; 


/**
 * Classe principal da aplicação Spring Boot.
 * Esta classe é responsável por iniciar a aplicação e configurar o ambiente Spring.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@SpringBootApplication
public class GoogolWebApplication {
	/**
	 * Construtor padrão da classe.
	 * Este construtor é necessário para a inicialização do Spring Boot.
	 */
	public GoogolWebApplication() {
	}

	/**
	 * Método principal que inicia a aplicação Spring Boot.
	 * @param args Argumentos da linha de comando.
	 */
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(GoogolWebApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.setLogStartupInfo(false);
		app.run(args);
	}

}
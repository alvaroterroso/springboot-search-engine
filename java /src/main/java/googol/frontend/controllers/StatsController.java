package googol.frontend.controllers;

import googol.frontend.BackendClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pela a página de estatísticas.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/stats")
public class StatsController {
    
    private final BackendClient backendClient;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * vai buscar a instancia do BackendClient para usar metodos da gateway
	 * @param backendClient Cliente do Backend
	*/
    public StatsController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }
	/**
	 * Método que apresenta o formulário de estatísticas.
	 * @return O nome da view HTML de estatísticas.
	*/
	@GetMapping
	public String statsForm(Model model) {  // Adiciona Model como parâmetro
		try {
			String rawStats = backendClient.getGateway().getFormattedStatistics();
			String formattedStats = rawStats.replace("\n", "<br>");
			model.addAttribute("stats", formattedStats);
			return "stats";
		} catch (Exception e) {
			model.addAttribute("error", "Erro ao carregar estatísticas.");
			return "error";
		}
	}

}
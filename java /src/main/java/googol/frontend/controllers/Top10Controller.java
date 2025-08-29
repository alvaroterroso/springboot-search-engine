package googol.frontend.controllers;

import googol.frontend.BackendClient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller para a página de top 10 de paginas com mais referências.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/top10")
public class Top10Controller {

    private final BackendClient backendClient;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * vai buscar a instancia do BackendClient para usar metodos da gateway
	 * @param backendClient Cliente do Backend
	*/
    public Top10Controller(BackendClient backendClient) {
        this.backendClient = backendClient;
    }


	/**
	 * Método que apresenta os 10 links com mais referências.	
	 * * @param model o modelo usado para enviar mensagens para a view
	 * @return O nome da view HTML de top 10.
	*/
    @GetMapping
    public String top10Form(Model model) {
        try {
            LinkedHashMap<String, Integer> rawStats = backendClient.getGateway().getTop10PagesByLinks();
            List<String> links = new ArrayList<>(rawStats.keySet()); // Apenas os links
            model.addAttribute("links", links);
            return "top10";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar estatísticas.");
            return "error";
        }
    }
}

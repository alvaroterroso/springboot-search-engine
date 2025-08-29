package googol.frontend.controllers;

import googol.frontend.BackendClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável pela página de pesquisa de páginas que contêm ligações para uma página específica.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/search-linksto")
public class LinksToController {
    
    private final BackendClient backendClient;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * @param backendClient Cliente do Backend
	 */
    public LinksToController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

	/**
	 * Método que apresenta o formulário de pesquisa de páginas com ligação para uma página específica.
	 * @return O nome da view HTML 
	 */
    @GetMapping
    public String searchlinksForm() {
        return "search-linksto";
    }

	/**
	 * Método para redirecionar o utilizador para a página de resultados com base na query introduzida.
	 * @param query a URL da página para pesquisar
	 * @param model o modelo usado para enviar mensagens para a view
	 * @return redireciona para a página de resultados com paginação 
	*/
    @PostMapping
    public String search(@RequestParam("query-links") String query, Model model) {
        return "redirect:/search-linksto/results?query-links=" + query + "&page=1";
    }
    
	/**
	 * Método que apresenta os resultados paginados da pesquisa que contêm ligação para a URL indicada.
	 * @param query a URL da página para pesquisar
	 * @param page o número da página atual
	 * @param model o modelo usado para enviar mensagens para a view
	 * @return O nome da view HTML com os resultados		
	 */
	
    @GetMapping("/results")
    public String searchPaginated(
            @RequestParam("query-links") String query,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        try {
            List<String> allResults = backendClient.getGateway().getPagesLinkingTo(query);
            int totalResults = allResults.size();
            int resultsPerPage = 10;
            int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);
            
            page = Math.max(1, Math.min(page, totalPages));
            
            int startIndex = (page - 1) * resultsPerPage;
            int endIndex = Math.min(startIndex + resultsPerPage, totalResults);
            List<String> pageResults = allResults.subList(startIndex, endIndex);
            
            model.addAttribute("resultsLinks", pageResults);
            model.addAttribute("queryLinks", query);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalResults", totalResults);
            model.addAttribute("startResult", startIndex + 1);
            model.addAttribute("endResult", endIndex);
            
            return "links-results";
        } catch (Exception e) {
            model.addAttribute("error", "Erro na pesquisa: " + e.getMessage());
            return "search-linksto";
        }
    }
}
package googol.frontend.controllers;

import googol.frontend.BackendClient;
import googol.frontend.model.AnalysisRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller para a página de pesquisa.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/search")
public class SearchController {
    
    private final BackendClient backendClient;

    private static final int RESULTS_PER_PAGE = 10;
	
	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * vai buscar a instancia do BackendClient para usar metodos da gateway
	 * @param backendClient Cliente do Backend
	 * */
    public SearchController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }
	
	@Autowired
	private WebSocketStatsController statsController;

	@Autowired
    private OpenAIController openAIController;

	/**
	 * Método que apresenta o formulário de pesquisa.
	 * @return O nome da view HTML de pesquisa.
	*/
    @GetMapping
    public String searchForm() {
        return "search";
    }
    
	/**
	 * Método que processa a pesquisa,  redireciona o utilizador para os resultados da pesquisa após submeter a query.
	 * @param query a string de pesquisa
	 * @param model o modelo usado para enviar mensagens para a view
	 * @return redireciona para a página de resultados com paginação
	*/
    @GetMapping("/do")
    public String search(@RequestParam String query) {
        return "redirect:/search/results?query=" + query + "&page=1&isPagination=false";
    }
    
	/**
	 * Método que apresenta os resultados paginados da pesquisa.
	 * @param query a string de pesquisa
	 * @param page o número da página atual
	 * @param isPagination indica se é mudança de página
	 * @param model o modelo usado para enviar mensagens para a view
	 * @return O nome da view HTML com os resultados
	*/
    @GetMapping("/results")
	public String searchPaginated(
			@RequestParam String query,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false, defaultValue = "false") boolean isPagination,
			Model model) {
		try {
			page = Math.max(1, page);
			
			List<String> pageResults = backendClient.getGateway().search(query, page, isPagination);
			
			if (pageResults.isEmpty() && page > 1) {
				return "redirect:/search/results?query=" + query + "&page=" + (page - 1);
			}
			
			boolean isLastPage = pageResults.size() < RESULTS_PER_PAGE;
			statsController.checkForUpdates();

			if (!pageResults.isEmpty()) {
                AnalysisRequest request = new AnalysisRequest();
                request.setQuery(query);
                request.setText(pageResults);
				String analysis = openAIController.generateAnalysis(request).getBody();
                model.addAttribute("analysis", analysis);
            }
			
			model.addAttribute("results", pageResults);
			model.addAttribute("query", query);
			model.addAttribute("currentPage", page);
			model.addAttribute("isFirstPage", page == 1);
			model.addAttribute("isLastPage", isLastPage);
			model.addAttribute("startResult", (page - 1) * RESULTS_PER_PAGE + 1);
			model.addAttribute("endResult", (page - 1) * RESULTS_PER_PAGE + pageResults.size());
			
			return "results";
		} catch (Exception e) {
			model.addAttribute("error", "Erro na pesquisa: " + e.getMessage());
			return "search";
		}
	}
}
package googol.frontend.controllers;

import googol.frontend.BackendClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável pela página que indexa URLs.
 * Este controlador lida com as requisições para adicionar URLs e exibir o formulário de adição.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/add-url")
public class AddUrlController {

    private final BackendClient backendClient;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * @param backendClient Cliente do Backend
	 */
    public AddUrlController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }
	
	@Autowired
	private WebSocketStatsController statsController;

	/**
	 * Método que mostra o formulário de adição de URLs.
	 * @return O nome da view HTML de adição de um novo URL.
	 */
    @GetMapping
    public String addUrlForm() {
        return "add-url";
    }
    
	/**
	 * Método que processa a adição de uma nova URL.
	 * @param url   a URL para ser adicionado
     * @param model o modelo usado para enviar mensagens para a view
     * @return nome da view HTML 
	 */
    @PostMapping
    public String addUrl(@RequestParam String url, Model model) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                model.addAttribute("error", "URL inválida. Deve começar com http:// ou https://");
                return "add-url";
            }
            
            backendClient.getGateway().putNew(url);
			statsController.checkForUpdates();
            
            model.addAttribute("success", "URL adicionada com sucesso: " + url);
            return "add-url";
            
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao adicionar URL: " + e.getMessage());
            return "add-url";
        }
    }
}
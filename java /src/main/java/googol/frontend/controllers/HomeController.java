package googol.frontend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Controller para a página inicial.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
public class HomeController {

	/**
	 * Método que apresenta a página inicial.
	 * @return O nome da view HTML da página inicial.
	 */
    @GetMapping("/")
    public String home() {
        return "index"; 
    }
}
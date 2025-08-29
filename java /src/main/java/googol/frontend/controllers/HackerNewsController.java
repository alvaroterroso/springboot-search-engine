package googol.frontend.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import googol.frontend.BackendClient;

import java.rmi.RemoteException;
import java.util.*;
import googol.frontend.model.IndexRequest;
import googol.frontend.model.HackerNewsStory;
import googol.frontend.model.IndexResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.stream.Collectors;


/**
 * Controller responsável pela página de Hacker News, onde 
 * o utilizador pode solicitar a indexação dos URLs das "top stories" 
 * do Hacker News que contenham os 
 * termos pesquisados no texto.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
@RequestMapping("/hackernews")
public class HackerNewsController {
	 private static final Logger log = LoggerFactory.getLogger(HackerNewsController.class);

    @Value("${hackernews.api.url}")
    private String hackerNewsApiUrl;

    private final RestTemplate restTemplate;
    private final BackendClient backendClient;
    private final Gson gson;

	@Autowired
	private WebSocketStatsController statsController;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * @param restTemplate o RestTemplate para fazer chamadas à API do Hacker News
	 * @param backendClient o cliente do Backend para indexação
	 */
    public HackerNewsController(RestTemplate restTemplate, 
                              BackendClient backendClient) {
        this.restTemplate = restTemplate;
        this.backendClient = backendClient;
        this.gson = new GsonBuilder().create();
    }

	/**
	 * Método que indexa histórias selecionadas do Hacker News no sistema de pesquisa.
     * @param request O pedido de indexação contendo IDs das histórias e termo de pesquisa
     * @return ResponseEntity com o estado da indexação e contagem de histórias indexadas
	 */
	
    @PostMapping("/index")
    @ResponseBody
    public ResponseEntity<IndexResponse> indexStories(@RequestBody IndexRequest request) {
        IndexResponse response = new IndexResponse();
        int indexedCount = 0;

        try {
            for (Integer storyId : request.getStoryIds()) {
                ResponseEntity<String> storyResponse = restTemplate.getForEntity(
                    hackerNewsApiUrl + "/item/" + storyId + ".json", 
                    String.class);

                if (storyResponse.getStatusCode().is2xxSuccessful()) {
                    HackerNewsStory story = gson.fromJson(storyResponse.getBody(), HackerNewsStory.class);
                    
                    if (story != null && story.getUrl() != null && story.matchesQuery(request.getQuery())) {
                        try {
                            backendClient.getGateway().putNew(story.getUrl());
                            indexedCount++;
							statsController.checkForUpdates();
                        } catch (RemoteException e) {
                            log.error("Failed to index story {}: {}", storyId, e.getMessage());
                        }
                    }
                }
            }

            if (indexedCount > 0) {
                response.setStatus("success");
                response.setMessage(indexedCount + " histórias indexadas com sucesso");
                response.setIndexedCount(indexedCount);
                return ResponseEntity.ok(response);
            } else {
                response.setStatus("error");
                response.setMessage("Nenhuma história válida encontrada para indexação");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error indexing stories: ", e);
            response.setStatus("error");
            response.setMessage("Erro durante a indexação: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
	/**
	 * Método que mostra a página de histórias do Hacker News.
	 * @param query a string de pesquisa
	 * @param limit o número máximo de histórias a serem exibidas
	 * @param model o modelo usado para enviar mensagens para a view
	 * @return O nome da view HTML de histórias do Hacker News.
	 */
    @GetMapping
    public String getStories(
            @RequestParam String query,
            @RequestParam(defaultValue = "30") int limit,
            Model model) {
        
        try {
            ResponseEntity<String> idsResponse = restTemplate.getForEntity(
                hackerNewsApiUrl + "/topstories.json", 
                String.class);
            
            int[] storyIds = gson.fromJson(idsResponse.getBody(), int[].class);
            
            List<HackerNewsStory> stories = Arrays.stream(storyIds)
                .limit(limit)
                .parallel() // Processamento paralelo, para mais eficiÊncia se necessário
                .mapToObj(this::fetchStory)
                .filter(Objects::nonNull)
                .filter(story -> story.matchesQuery(query))
                .collect(Collectors.toList());
            
            // 3. Return response
            model.addAttribute("stories", stories);
            model.addAttribute("searchQuery", query);
            return "hackernews"; 
            
        } catch (Exception e) {
            log.error("Error fetching Hacker News stories: ", e);
            model.addAttribute("error", "Error fetching stories: " + e.getMessage());
            return "error";
        }
    }

	/**
	 * Método que obtém os detalhes de uma história específica do Hacker News através do seu ID.
     * @param storyId O ID da história 
     * @return Objeto HackerNewsStory com os detalhes da história, ou null se não for encontrada
     */
    private HackerNewsStory fetchStory(int storyId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                hackerNewsApiUrl + "/item/" + storyId + ".json", 
                String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return gson.fromJson(response.getBody(), HackerNewsStory.class);
            }
        } catch (Exception e) {
            log.error("Error fetching story {}: {}", storyId, e.getMessage());
        }
        return null;
    }
}
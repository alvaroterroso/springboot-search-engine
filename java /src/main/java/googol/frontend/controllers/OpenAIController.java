package googol.frontend.controllers;

import com.google.gson.*;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import googol.frontend.model.AnalysisRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.util.*;

import java.nio.charset.StandardCharsets;


/**
 * Controller responsável pela comunicação com a API do Gemini
 * para gerar análise textual baseada nos termos da pesquisa e nas descrições do seu resultado.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
@Controller
public class OpenAIController {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

	/**
	 * Método que processa a requisição para gerar uma análise textual
	 * com base nas descrições fornecidos e na query de pesquisa.
	 * @param request objeto que contém a query e as descrições.
	 * @return  a resposta da API do Gemini com a análise.
	 */
    @PostMapping("/generate-analysis")
    @ResponseBody
    public ResponseEntity<String> generateAnalysis(@RequestBody AnalysisRequest request) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(geminiApiUrl + "?key=" + apiKey);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            StringBuilder resultBlock = new StringBuilder();
            List<String> snippets = request.getText();
            
            for (int i = 0; i < snippets.size(); i++) {
                resultBlock.append("Resultado ")
                         .append(i + 1)
                         .append("\nDescrição: ")
                         .append(snippets.get(i))
                         .append("\n\n");
            }

            String prompt = "Gera um sumário curto, claro e informativo com base nas descrições das páginas web listadas abaixo. " +
                    "O texto deve servir como introdução contextual à página de resultados da pesquisa do Googol. " +
                    "Deve parecer um parágrafo introdutório típico no topo de uma página de pesquisa, ajudando o utilizador a compreender rapidamente o tema predominante. " +
                    "Baseia-te nos termos mais relevantes da pesquisa e nas frases mais significativas extraídas dos resultados. " +
                    "Evita linguagem de diálogo, perguntas ou qualquer forma de interação direta com o utilizador. " +
                    "Apresenta apenas um resumo direto, neutro e informativo.\n\n" +
                    "Termo pesquisado: " + request.getQuery() + "\n\n" +
                    resultBlock.toString();

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonObject message = new JsonObject();
            JsonArray parts = new JsonArray();
            parts.add(textPart);
            message.add("parts", parts);

            JsonObject content = new JsonObject();
            JsonArray contents = new JsonArray();
            contents.add(message);
            content.add("contents", contents);

            httpPost.setEntity(new StringEntity(content.toString(), StandardCharsets.UTF_8));

            String response = EntityUtils.toString(client.execute(httpPost).getEntity());
            JsonObject responseJson = JsonParser.parseString(response).getAsJsonObject();

            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonObject contentObj = firstCandidate.getAsJsonObject("content");
                JsonArray partsArray = contentObj.getAsJsonArray("parts");
                String analysis = partsArray.get(0).getAsJsonObject().get("text").getAsString();
                return ResponseEntity.ok(analysis);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Não foi possível gerar análise com a API do Gemini.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao comunicar com a API do Gemini: " + e.getMessage());
        }
    }
}
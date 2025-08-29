package googol.frontend.controllers;

import googol.frontend.BackendClient;
import googol.frontend.model.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * Controller para  a pagina dos websocket stats, apresenta
 * informações gerais em tempo real
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */

@Controller
public class WebSocketStatsController {

	private final BackendClient backendClient;

	/**
	 * Construtor da classe, recebe dados para a inicialização dos atributos
	 * vai buscar a instancia do BackendClient para usar metodos da gateway
	 * @param backendClient Cliente do Backend
	 */
	public WebSocketStatsController(BackendClient backendClient) {
        this.backendClient = backendClient;
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

	/**
	 * Método auxilar que envia as estatísticas formatadas para o tópico WebSocket "/topic/stats".
	 * @param stats As estatísticas formatadas a serem enviadas.
	 */
	private void sendStatsUpdate(String stats) {
		//System.out.println(">> A enviar para /topic/stats: " + stats); -> DEBUG
        Stats statsObj = new Stats();
        statsObj.setLastUpdated(new Date());
        statsObj.setFormattedStats(stats);
        messagingTemplate.convertAndSend("/topic/stats", statsObj);
    }
	/**
	 * Método que verifica se há atualizações de estatísticas e envia os dados atualizados para os clientes.
	 * Este método pode ser invocado por outros controllers de modo a atualizar a página em tempo real.
	 */
	public void checkForUpdates() {
        try {
            if (backendClient.getGateway() != null) {
                String currentStats = backendClient.getGateway().getFormattedStatistics();
                sendStatsUpdate(currentStats);
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar atualizações: " + e.getMessage());
        }
    }

	/**
	 * Método que responde a mensagens WebSocket enviadas para o tópico "/topic/stats".
	 * Este método é invocado quando um cliente se conecta ao WebSocket e solicita atualizações de estatísticas.
	 */
	@MessageMapping("/topic/stats")
	public void updateStats() {
		try {
			if (backendClient.getGateway() != null) {
				System.out.println(">> updateStats() foi chamado");
				String currentStats = backendClient.getGateway().getFormattedStatistics();
				sendStatsUpdate(currentStats);
			}
		} catch (Exception e) {
			System.err.println("Erro ao responder update-stats: " + e.getMessage());
		}
	}
	
	/**
	 * Método que apresenta a página de estatísticas.
	 * @return O nome da view HTML de estatísticas.
	 */
	@GetMapping("/estatisticas")
	public String showStatsPage() {
		return "webstats";
	}

	/**
	 * Método para obter as estatísticas quando a página é carregada a primeira vez.
	 * @return As estatísticas formatadas.
	 */
    public Stats getStats() {
        Stats stats = new Stats();
        try {
            if (backendClient.getGateway() != null) {
                stats.setFormattedStats(backendClient.getGateway().getFormattedStatistics());
                stats.setLastUpdated(new Date());
            }
        } catch (Exception e) {
            stats.setFormattedStats("Erro ao obter estatísticas: " + e.getMessage());
        }
		//System.out.println("Stats: " + stats.getFormattedStats()); -> DEBUG
        return stats;
    }
}
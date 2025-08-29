package googol.frontend.model;

import java.util.Date;

/**
 * Classe que representa as estatísticas do sistema.
 * Contém informações como a data da última atualização e as estatísticas formatadas.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class Stats {
    private Date lastUpdated;
    private String formattedStats;

	/**
	 * Método para obter a data da última atualização.
	 */
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
	/**
	 * Método para definir a data da última atualização.
	 * @param lastUpdated A data da última atualização.
	 */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
	/**
	 * Método para obter as estatísticas formatadas.
	 * @return As estatísticas formatadas.
	 */
    public String getFormattedStats() {
        return formattedStats;
    }

	/**
	 * Método para definir as estatísticas formatadas.
	 * @param formattedStats As estatísticas formatadas.
	 */
    public void setFormattedStats(String formattedStats) {
        this.formattedStats = formattedStats;
    }
}
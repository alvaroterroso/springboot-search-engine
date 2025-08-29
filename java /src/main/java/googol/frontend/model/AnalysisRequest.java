package googol.frontend.model;

import java.util.List;

/**
 * Classe que representa um pedido de análise de uma página.
 * Contém a query e as descrições associados.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */

public class AnalysisRequest {
    private String query;
    private List<String> text;

	/**
	 * Construtor da classe AnalysisRequest.
	 */
    public AnalysisRequest() {
    }
	/**
	 * Método para obter a pesquisa.
	 * @return A string de pesquisa.
	 */
    public String getQuery() {
        return query;
    }
	
	/**
	 * Método para definir a pesquisa.
	 * @param query A string de pesquisa.
	 */
    public void setQuery(String query) {
        this.query = query;
    }
	/**
	 * Método para obter as descrições.
	 * @return A lista de descrições.
	 */
    public List<String> getText() {
        return text;
    }

	/**
	 * Método para definir as descrições.
	 * @param text A lista de descrições.
	 */
    public void setText(List<String> text) {
        this.text = text;
    }
}

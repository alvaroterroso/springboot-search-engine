package googol.frontend.model;

import java.util.List;

/**
 * Classe que representa um pedido de indexação.
 * Contém os IDs das histórias do Hacker News a serem indexadas 
 * e a query (termo de pesquisa) associada à filtragem das histórias.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class IndexRequest {
    private List<Integer> storyIds;
    private String query;

	/**
	 * Construtor da classe IndexRequest.
	 */
    public IndexRequest() {
    }
	/**
	 * Método para obter os IDs das histórias a ser indexadas.
	 * @return A lista de IDs das histórias.
	 */
    public List<Integer> getStoryIds() {
        return storyIds;
    }

	/**
	 * Método para definir os IDs das histórias a serem indexadas.
	 * @param storyIds A lista de IDs das histórias.
	 */
    public void setStoryIds(List<Integer> storyIds) {
        this.storyIds = storyIds;
    }

	/**
	 * Método para obter a query.
	 * @return A string de pesquisa.
	 */
    public String getQuery() {
        return query;
    }

	/**
	 * Método para definir a query.
	 * @param query A string de pesquisa.
	 */
    public void setQuery(String query) {
        this.query = query;
    }
}
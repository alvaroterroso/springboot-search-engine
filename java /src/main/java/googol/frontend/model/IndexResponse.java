package googol.frontend.model;

/**
 *  Classe que representa a resposta a um pedido de indexação de histórias do Hacker News.
 *  Contém informações sobre o estado da operação, uma mensagem descritiva,
 *  e o número de histórias que foram efetivamente indexadas.
 *  @author Álvaro Terroso
 *  @author Mariana Sousa
 *  @version 1.0
 */
public class IndexResponse {
    private String status;
    private String message;
    private int indexedCount;

	/**
	 * Construtor da classe IndexResponse.
	 */
    public IndexResponse() {
    }
	
	/**
	 * Método para obter o estado da operação.
	 * @return O estado da operação.
	 */
    public String getStatus() {
        return status;
    }

	/**
	 * Método para definir o estado da operação.
	 * @param status O estado da operação.
	 */
    public void setStatus(String status) {
        this.status = status;
    }

	/**
	 * Método para obter a mensagem descritiva.
	 * @return A mensagem descritiva.
	 */
	public String getMessage() {
        return message;
    }

	/**
	 * Método para definir a mensagem descritiva.
	 * @param message A mensagem descritiva.
	 */
    public void setMessage(String message) {
        this.message = message;
    }
	
	/**
	 * Método para obter o número de histórias indexadas.
	 * @return O número de histórias indexadas.
	 */
	public int getIndexedCount() {
		return indexedCount;
	}

	/**
	 * Método para definir o número de histórias indexadas.
	 * @param indexedCount O número de histórias indexadas.
	 */
	public void setIndexedCount(int indexedCount) {
		this.indexedCount = indexedCount;
	}
}
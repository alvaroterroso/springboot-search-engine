package googol.frontend.model;

/**
 * Classe que representa uma noticia do Hacker News.
 * Contém informações como ID, título, URL, texto, pontuação e tempo.
 * @author Álvaro Terroso
 * @author Mariana Sousa
 * @version 1.0
 */
public class HackerNewsStory {
    private int id;
    private String title;
    private String url;
    private String text;
    private int score;
    private long time;

	/**
	 * Construtor da classe HackerNewsStory.
	 */
    public HackerNewsStory() {
    }

	/**
	 * Método para obter o ID.
	 * @return O ID do Hacker News.
	 */
    public int getId() {
        return id;
    }
	/**
	 * Método para definir o ID.
	 * @param id O ID do Hacker News.
	 */
    public void setId(int id) {
        this.id = id;
    }

	/**
	 * Método para obter o título.
	 * @return O título do Hacker News.
	 */
	public String getTitle() {
        return title;
    }

	/**
	 * Método para definir o título.
	 * @param title O título do Hacker News.
	 */
    public void setTitle(String title) {
        this.title = title;
    }

	/**
	 * Método para obter a URL.
	 * @return A URL do Hacker News.
	 */
	public String getUrl() {
        return url;
    }

	/**
	 * Método para definir a URL.
	 * @param url A URL do Hacker News.
	 */
    public void setUrl(String url) {
        this.url = url;
    }

	/**
	 * Método para obter o texto.
	 * @return O texto do Hacker News.
	 */
	public String getText() {
        return text;
    }

	/**
	 * Método para definir o texto.
	 * @param text O texto do Hacker News.
	*/
    public void setText(String text) {
        this.text = text;
    }

	/**
	 * Método para obter a pontuação.
	 * @return A pontuação do Hacker News.
	 */
	public int getScore() {
        return score;
    }

	/**
	 * Método para definir a pontuação.
	 * @param score A pontuação do Hacker News.
	 */
    public void setScore(int score) {
        this.score = score;
    }

	/**
	 * Método para obter o tempo.
	 * @return O tempo do Hacker News.
	 */
	public long getTime() {
        return time;
    }

	/**
	 * Método para definir o tempo.
	 * @param time O tempo do Hacker News.
	 */
    public void setScore(long time) {
        this.time = time;
    }

	
	/**
	 * Método que verifica se a história contém o termo de pesquisa 
	 * no título ou no texto da mesma.
	 * @param query A pesquisa a ser verificada.
	 * @return true se o termo de pesquisa estiver presente, false caso contrário.
	 */
    public boolean matchesQuery(String query) {
        if (query == null || query.isEmpty()) return true;
        String lowercaseQuery = query.toLowerCase();
        return (title != null && title.toLowerCase().contains(lowercaseQuery)) ||
               (text != null && text.toLowerCase().contains(lowercaseQuery));
    }
}
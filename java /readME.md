# Googol 

Nesta fase do projeto, desenvolvemos a componente de frontend recorrendo ao framework Spring Boot, integrando de forma intuitiva as funcionalidades implementadas na etapa anterior numa interface web acessível ao utilizador. Para garantir a atualização em tempo real das estatísticas do sistema, utilizámos WebSockets, permitindo uma comunicação eficiente entre o cliente e o servidor. Adicionalmente, integrámos APIs externas, nomeadamente a da HackerNews e a do Gemini, que oferecem funcionalidades complementares ao sistema.


# Manual de Instalação

1. Para correr este código é necessário a instalação de um IDE que seja compatível com a linguagem Java, no caso deste projeto foi utilizado o VS Code, pode proceder à instalação deste software a partir deste link: [Vscode] https://code.visualstudio.com/download

2. Utilizamos também a biblioteca [Jsoup] https://jsoup.org/ , para facilitar o scraping das páginas web.

3. Deve também instalar o Java na sua máquina [Java] https://www.java.com/en/download/manual.jsp

4. Deve instalar finalmente o [Docker] https://www.docker.com e correr com o DevContainer

5. Instalar o Maven (MacOs: brew install maven & mvn -v). Depois dirigir-se para a diretoria onde está o ficheiro pom.xml e correr: mvn clean install ou através do link [Maven](https://maven.apache.org/download.cgi).

6. Instalar o [SpringBoot](https://start.spring.io/) e escolher todas as dependências necessárias
	- Spring Web.
    - Thymeleaf.
    - Spring Boot DevTools
    - WebSocket
	- JSON
	- Tomcat Embedded
	- Jsoup
	- WebJars SockJS e STOMP Websocket
	- Gson
	- Apache HttpClient

# Como executar o programa

1. Abra o ficheiro "config.properties" que está localizado na pasta principal do projeto e altere-o para as definições adequadas (se deseja correr o código em duas máquinas, o parametro gatewayIP deve corresponder com o IP da máquina que irá correr a gateway). Para descobrir estes dados basta executar estes comando no terminal, dependendo do Sistema Operativo:

 Windows:
    ```bash
    ipconfig 
    ```
    Unix based systems
    ```bash
    ifconfig 

Estamos a utilizar também duas APIs: [Gemini](https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent) e [HackerNews](https://hacker-news.firebaseio.com/v0)

2. Deverá de seguida abrir no mínimo 4 terminais (1 Gateway, 1 Downloader e 1 Barrel), utilizando os seguintes comandos:

Gateway: java -cp "<o-seu-diretorio>/target/lib/jsoup-1.18.3.jar:." /<o-seu-diretorio>/src/main/java/googol/Gateway.java
Barrel: java -cp "<o-seu-diretorio>/target/lib/jsoup-1.18.3.jar:." /<o-seu-diretorio>/src/main/java/googol/Barrel.java
Downloader: java -cp "<o-seu-diretorio>/target/lib/jsoup-1.18.3.jar:." /<o-seu-diretorio>/src/main/java/googol/Downloader.java

ou comandos que estao no run-commands.sh

Para descobrir o caminho para um ficheiro expecífico, basta clicar com o botão direito do rato em cima do ficheiro, e selecionar Copy Path. No caso esta informação é útil para descobrir o caminho da livraria jsoup.

Para correr o frontend, basta aceder à diretoria onde se encontra o ficheiro pom.xml, e executar os comandos:

mvn clean install
mvn spring-boot:run

3. No terminal do Client terá o menu de acesso ao programa, onde pode interagir diretamente com as funcionalidades do mesmo. - Meta 1

4. De notar que os Barrels possuem um mecanismo de memória, onde sempre que são encerrados, guardam a informação num ficheiro .txt, com o seu determinado número. Se deseja começar a execução do 0, apague o conteúdo destes ficheiros.

5. Deve também iniciar sessão na API do [Gemini](https://ai.google.dev/gemini-api/docs/api-key?hl=pt-br) de modo a conseguir uma dev key, para colocar no ficheiro application.properties no campo gemini.api.key.

6. O cliente corre em localhost:8080, lá terá acesso ao menu e todas as funcionalidades do código.

# Autores
Álvaro Terroso & Mariana Sousa
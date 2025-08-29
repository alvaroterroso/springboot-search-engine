var stompClient = null;
var subscription = null;

function connect() {
    var socket = new SockJS('/stats-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        document.getElementById('connection-status').className = 'badge bg-success';
        document.getElementById('connection-status').textContent = 'Conectado';
        subscribeToStats();
        requestStatsUpdate();
    });
}

function subscribeToStats() {
	if (subscription) subscription.unsubscribe();
    subscription = stompClient.subscribe('/topic/stats', function (message) {
		console.log("Mensagem recebida no /topic/stats:", message.body);
        const stats = JSON.parse(message.body);
        updateStatsDisplay(stats);
    });
}

function requestStatsUpdate() {
	console.log("A enviar pedido de atualização");
    stompClient.send("/app/topic/stats", {}, {});
}

function updateStatsDisplay(stats) {
    const content = document.getElementById('stats-content');
	console.log("Atualizando exibição de estatísticas:", stats);
    
    content.innerHTML = `
        <div class="mb-3">
            <small class="text-muted">Última atualização: ${new Date(stats.lastUpdated).toLocaleString()}</small>
        </div>
        <pre class="bg-light p-3 rounded">${stats.formattedStats}</pre>
    `
}

window.addEventListener('load', connect);
window.addEventListener('beforeunload', function() {
    if (stompClient !== null) {
        stompClient.disconnect();
        document.getElementById('connection-status').className = 'badge bg-danger';
        document.getElementById('connection-status').textContent = 'Desconectado';
    }
});
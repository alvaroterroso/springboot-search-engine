document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('indexSelected')?.addEventListener('click', function () {
        const selected = Array.from(document.querySelectorAll('.story-checkbox:checked'))
                             .map(checkbox => parseInt(checkbox.value));
        
        if (selected.length === 0) {
            document.getElementById('indexStatus').innerHTML = 
                '<div class="alert alert-warning">Please select at least one story</div>';
            return;
        }

        fetch('/hackernews/index', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                storyIds: selected,
                query: document.querySelector('h1 span').textContent
            })
        })
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            const statusDiv = document.getElementById('indexStatus');
            statusDiv.innerHTML = data.status === 'success' 
                ? `<div class="alert alert-success">${data.message}</div>`
                : `<div class="alert alert-danger">${data.message}</div>`;
        })
        .catch(error => {
            document.getElementById('indexStatus').innerHTML = 
                `<div class="alert alert-danger">Error: ${error.message}</div>`;
        });
    });
});

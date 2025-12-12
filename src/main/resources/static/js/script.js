document.addEventListener('DOMContentLoaded', () => {
    console.log('Early Child Learn App Loaded!');

    // Add simple click effects or global logic here
    const buttons = document.querySelectorAll('button, .card, .grid-item');
    buttons.forEach(btn => {
        btn.addEventListener('mousedown', () => {
            btn.style.transform = 'scale(0.95)';
        });
        btn.addEventListener('mouseup', () => {
            btn.style.transform = '';
        });
        btn.addEventListener('mouseleave', () => {
            btn.style.transform = '';
        });
    });
});

// System Heartbeat Check
setInterval(() => {
    fetch('/heartbeat')
        .then(response => {
            if (response.ok) {
                console.log('System heartbeat: OK');
            } else {
                console.warn('System heartbeat: Server returned error', response.status);
            }
        })
        .catch(error => {
            console.error('System heartbeat: Connection failed', error);
        });
}, 60000);

function speak(text, lang = 'zh-TW') {
    if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = lang;
        window.speechSynthesis.speak(utterance);
    }
}

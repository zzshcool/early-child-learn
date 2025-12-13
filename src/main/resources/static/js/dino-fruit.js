const fruits = [
    { name: 'apple', img: '/images/games/dino-fruit/apple.png', label: '蘋果', emoji: '🍎' },
    { name: 'banana', img: '/images/games/dino-fruit/banana.png', label: '香蕉', emoji: '🍌' },
    { name: 'grape', img: '/images/games/dino-fruit/grape.png', label: '葡萄', emoji: '🍇' },
    { name: 'orange', img: '/images/games/dino-fruit/orange.png', label: '橘子', emoji: '🍊' }
];

let currentRequest = null;
let score = 0;
const totalRounds = 5;
const dinosaur = document.getElementById('dino-img');
const bubble = document.getElementById('bubble');
const fruitsContainer = document.getElementById('fruits-container');
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    if (audioCtx.state === 'suspended') audioCtx.resume();
    nextRound();
}

function playSound(type) {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (type === 'eat') {
        osc.frequency.setValueAtTime(200, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(100, audioCtx.currentTime + 0.2);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.2);
        osc.type = 'sawtooth';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.2);
    } else if (type === 'wrong') {
        osc.frequency.setValueAtTime(150, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(100, audioCtx.currentTime + 0.3);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        osc.type = 'square';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.3);
    } else if (type === 'end') {
        // Simple Win Jingle
        const notes = [400, 500, 600, 800];
        notes.forEach((f, i) => {
            const o = audioCtx.createOscillator();
            const g = audioCtx.createGain();
            o.connect(g);
            g.connect(audioCtx.destination);
            o.frequency.value = f;
            g.gain.value = 0.1;
            g.gain.linearRampToValueAtTime(0, audioCtx.currentTime + i * 0.2 + 0.3);
            o.start(audioCtx.currentTime + i * 0.2);
            o.stop(audioCtx.currentTime + i * 0.2 + 0.3);
        });
    }
}

function nextRound() {
    if (score >= totalRounds) {
        playSound('end');
        document.getElementById('message-overlay').classList.remove('hidden');
        return;
    }

    currentRequest = fruits[Math.floor(Math.random() * fruits.length)];

    bubble.textContent = `我想吃${currentRequest.label}! ${currentRequest.emoji}`;
    bubble.classList.remove('hidden');

    renderOptions();
}

function renderOptions() {
    fruitsContainer.innerHTML = '';
    // Shuffle options
    const options = [...fruits].sort(() => Math.random() - 0.5);

    options.forEach(fruit => {
        const div = document.createElement('div');
        div.className = 'fruit-option';
        const img = document.createElement('img');
        img.src = fruit.img;
        div.appendChild(img);

        div.onclick = () => checkAnswer(fruit);
        fruitsContainer.appendChild(div);
    });
}

function checkAnswer(fruit) {
    if (fruit.name === currentRequest.name) {
        playSound('eat');
        dinosaur.classList.add('eating');
        bubble.classList.add('hidden');
        score++;

        setTimeout(() => {
            dinosaur.classList.remove('eating');
            nextRound();
        }, 1000);
    } else {
        playSound('wrong');
        dinosaur.classList.add('shake');
        setTimeout(() => dinosaur.classList.remove('shake'), 500);
    }
}

let score = 0;
let isFishing = false;
let gameActive = false;
const scoreElement = document.getElementById('score');
const line = document.getElementById('line');
const tutorial = document.getElementById('tutorial-overlay');
const container = document.getElementById('game-container');
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function startGame() {
    tutorial.classList.add('hidden');
    gameActive = true;
    if (audioCtx.state === 'suspended') audioCtx.resume();
}

function playSound(type) {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (type === 'cast') {
        osc.frequency.setValueAtTime(300, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(100, audioCtx.currentTime + 0.3);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.3);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.3);
    } else if (type === 'catch') {
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(800, audioCtx.currentTime + 0.2);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.5);
        osc.type = 'triangle';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.5);
    }
}

container.addEventListener('click', () => {
    if (!gameActive || isFishing) return;

    isFishing = true;
    playSound('cast');

    // Cast line
    line.style.height = '300px';

    // Random wait time 1-3 seconds
    const waitTime = Math.random() * 2000 + 1000;

    setTimeout(() => {
        // Catch fish!
        catchFish();
    }, waitTime);
});

function catchFish() {
    playSound('catch');

    // Create fish visuals
    const fish = document.createElement('img');
    fish.src = '/images/games/cat-fishing/fish.png';
    fish.classList.add('fish');

    // Position near the hook
    const hookRect = document.getElementById('hook').getBoundingClientRect();
    // fish.style.left = (hookRect.left) + 'px';
    // fish.style.top = (hookRect.top) + 'px';

    // Actually easier to just append to line
    document.getElementById('cat-container').appendChild(fish);
    fish.style.position = 'absolute';
    fish.style.left = '160px'; // Approx hook pos relative to container
    fish.style.top = '150px';

    fish.classList.add('caught');

    score++;
    scoreElement.textContent = score;

    // Reset line
    setTimeout(() => {
        line.style.height = '100px';
        fish.remove();
        isFishing = false;
    }, 1000);
}

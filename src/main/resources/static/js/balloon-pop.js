let score = 0;
let gameActive = false;
let spawnInterval = null;
const targetScore = 15;
const balloonsArea = document.getElementById('balloons-area');
const scoreElement = document.getElementById('score');
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

const balloonColors = ['red', 'blue', 'yellow', 'green', 'purple', 'pink'];

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    gameActive = true;
    if (audioCtx.state === 'suspended') audioCtx.resume();

    // 開始生成氣球
    spawnBalloon();
    spawnInterval = setInterval(() => {
        if (gameActive) {
            spawnBalloon();
        }
    }, 1500); // 每 1.5 秒生成一個氣球
}

function spawnBalloon() {
    const balloon = document.createElement('div');
    balloon.className = 'balloon';

    // 隨機顏色
    const color = balloonColors[Math.floor(Math.random() * balloonColors.length)];
    balloon.classList.add(color);

    // 隨機水平位置
    const leftPos = Math.random() * (window.innerWidth - 100);
    balloon.style.left = leftPos + 'px';
    balloon.style.bottom = '-120px';

    // 隨機左右漂移距離
    const drift = (Math.random() - 0.5) * 100;
    balloon.style.setProperty('--drift', drift + 'px');

    // 點擊事件
    balloon.addEventListener('click', () => popBalloon(balloon, color));
    balloon.addEventListener('touchstart', (e) => {
        e.preventDefault();
        popBalloon(balloon, color);
    });

    balloonsArea.appendChild(balloon);

    // 6 秒後自動移除（動畫時長）
    setTimeout(() => {
        if (balloon.parentElement) {
            balloon.remove();
        }
    }, 6000);
}

function popBalloon(balloon, color) {
    if (!gameActive || balloon.classList.contains('popped')) return;

    balloon.classList.add('popped');
    playPopSound();
    createParticles(balloon, color);

    score++;
    scoreElement.textContent = score;

    // 移除氣球
    setTimeout(() => {
        balloon.remove();
    }, 300);

    // 檢查是否完成
    if (score >= targetScore) {
        endGame();
    }
}

function createParticles(balloon, color) {
    const rect = balloon.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;

    // 生成 8 個粒子
    for (let i = 0; i < 8; i++) {
        const particle = document.createElement('div');
        particle.className = 'pop-particle';

        // 粒子顏色與氣球相同
        const colorMap = {
            'red': '#FF6B6B',
            'blue': '#4ECDC4',
            'yellow': '#FFE66D',
            'green': '#95E1D3',
            'purple': '#C78EFF',
            'pink': '#FF85C0'
        };
        particle.style.setProperty('--particle-color', colorMap[color]);

        // 計算粒子飛散方向
        const angle = (Math.PI * 2 * i) / 8;
        const distance = 50 + Math.random() * 30;
        const tx = Math.cos(angle) * distance;
        const ty = Math.sin(angle) * distance;

        particle.style.setProperty('--tx', tx + 'px');
        particle.style.setProperty('--ty', ty + 'px');

        particle.style.left = centerX + 'px';
        particle.style.top = centerY + 'px';

        document.body.appendChild(particle);

        // 動畫結束後移除
        setTimeout(() => particle.remove(), 600);
    }
}

function playPopSound() {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    osc.frequency.setValueAtTime(800, audioCtx.currentTime);
    osc.frequency.linearRampToValueAtTime(200, audioCtx.currentTime + 0.1);
    gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
    gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.15);

    osc.type = 'square';
    osc.start();
    osc.stop(audioCtx.currentTime + 0.15);
}

function playWinSound() {
    const notes = [523.25, 659.25, 783.99, 1046.50]; // C E G C
    notes.forEach((freq, i) => {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.connect(gain);
        gain.connect(audioCtx.destination);

        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime + i * 0.15);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + i * 0.15 + 0.3);

        osc.start(audioCtx.currentTime + i * 0.15);
        osc.stop(audioCtx.currentTime + i * 0.15 + 0.3);
    });
}

function endGame() {
    gameActive = false;
    clearInterval(spawnInterval);
    playWinSound();

    setTimeout(() => {
        document.getElementById('message-overlay').classList.remove('hidden');
    }, 500);
}

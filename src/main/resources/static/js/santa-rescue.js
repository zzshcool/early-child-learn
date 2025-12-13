document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('game-container');
    const scoreElement = document.getElementById('score');
    const timeElement = document.getElementById('time');
    const overlay = document.getElementById('message-overlay');
    const messageText = document.getElementById('message-text');
    const bubbleText = document.querySelector('#bubble');

    // Game Config
    const giftImages = [
        '/images/games/santa-rescue/gift-red.png',
        '/images/games/santa-rescue/gift-green.png',
        '/images/games/santa-rescue/gift-blue.png',
        '/images/games/santa-rescue/gift-gold.png'
    ];

    let score = 0;
    let timeLeft = 10;
    let gameActive = false;
    let spawnInterval;
    let timerInterval;

    // Sound effects based on previous context
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

    function playCollectSound() {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(600, audioCtx.currentTime);
        oscillator.frequency.exponentialRampToValueAtTime(1200, audioCtx.currentTime + 0.1);
        gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.1);
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.1);
    }

    function playWinSound() {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const notes = [523.25, 659.25, 783.99, 1046.50]; // C E G C
        notes.forEach((note, index) => {
            const oscillator = audioCtx.createOscillator();
            const gainNode = audioCtx.createGain();
            oscillator.type = 'triangle';
            oscillator.frequency.value = note;
            gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime + index * 0.2);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + index * 0.2 + 0.3);
            oscillator.connect(gainNode);
            gainNode.connect(audioCtx.destination);
            oscillator.start(audioCtx.currentTime + index * 0.2);
            oscillator.stop(audioCtx.currentTime + index * 0.2 + 0.3);
        });
    }

    function createSparkle(x, y) {
        for (let i = 0; i < 5; i++) {
            const sparkle = document.createElement('div');
            sparkle.classList.add('sparkle');
            sparkle.style.left = (x + (Math.random() - 0.5) * 50) + 'px';
            sparkle.style.top = (y + (Math.random() - 0.5) * 50) + 'px';
            container.appendChild(sparkle);
            setTimeout(() => sparkle.remove(), 600);
        }
    }

    function spawnGift() {
        if (!gameActive) return;

        const gift = document.createElement('img');
        gift.src = giftImages[Math.floor(Math.random() * giftImages.length)];
        gift.classList.add('gift');

        // Random position
        const x = Math.random() * (window.innerWidth - 120) + 10;
        const y = Math.random() * (window.innerHeight - 250) + 100;

        gift.style.left = `${x}px`;
        gift.style.top = `${y}px`;
        // Make it appear immediately
        gift.style.transform = 'scale(0)';
        gift.style.transition = 'transform 0.2s';

        // Touch/Click handler
        const collectHandler = (e) => {
            e.preventDefault();
            if (!gameActive || gift.classList.contains('collected')) return;
            collectGift(e, gift);
        };

        gift.addEventListener('click', collectHandler);
        gift.addEventListener('touchstart', collectHandler);

        container.appendChild(gift);

        // Pop in animation
        requestAnimationFrame(() => {
            gift.style.transform = 'scale(1)';
        });

        // Auto remove after 1 second (1000ms)
        const removeTimer = setTimeout(() => {
            if (gift.parentNode && !gift.classList.contains('collected')) {
                gift.style.transform = 'scale(0)';
                setTimeout(() => gift.remove(), 200);
            }
        }, 1000); // 1 Second lifetime requirement

        // Cleanup timer if collected
        gift.dataset.timerId = removeTimer;
    }

    function collectGift(e, gift) {
        clearTimeout(gift.dataset.timerId); // Stop auto-remove
        gift.classList.add('collected');
        playCollectSound();

        // Use clientX/Y for click, or center of element for fallback
        let cx = e.clientX;
        let cy = e.clientY;
        if (!cx && e.touches && e.touches.length > 0) {
            cx = e.touches[0].clientX;
            cy = e.touches[0].clientY;
        }
        createSparkle(cx, cy);

        score++;
        scoreElement.textContent = score;

        setTimeout(() => gift.remove(), 500);
    }

    function startGame() {
        score = 0;
        timeLeft = 10;
        gameActive = true;

        scoreElement.textContent = score;
        timeElement.textContent = timeLeft;
        overlay.classList.add('hidden');

        // Clear any existing gifts
        document.querySelectorAll('.gift').forEach(el => el.remove());

        // Spawn a gift every 0.8 seconds (slightly faster than lifetime to ensure density)
        spawnInterval = setInterval(spawnGift, 800);

        timerInterval = setInterval(() => {
            timeLeft--;
            timeElement.textContent = timeLeft;
            if (timeLeft <= 0) {
                endGame();
            }
        }, 1000);

        // Initial spawn
        spawnGift();
    }

    function endGame() {
        gameActive = false;
        clearInterval(spawnInterval);
        clearInterval(timerInterval);
        showResult();
    }

    function showResult() {
        playWinSound();
        messageText.textContent = `時間到! 得分: ${score}`;
        bubbleText.textContent = `太棒了!`;
        overlay.classList.remove('hidden');
        createSnow();
    }

    function createSnow() {
        // Simple snow effect reuse
        const flake = document.createElement('div');
        flake.classList.add('snowflake');
        flake.innerHTML = '❄';
        flake.style.left = Math.random() * 100 + 'vw';
        flake.style.animationDuration = Math.random() * 3 + 2 + 's';
        flake.style.fontSize = Math.random() * 20 + 10 + 'px';
        container.appendChild(flake);
        setTimeout(() => flake.remove(), 5000);
    }

    // Start loop for snow in background regardless of game state
    setInterval(createSnow, 300);

    // Initial Start
    startGame();
});

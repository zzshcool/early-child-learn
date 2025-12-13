document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('game-container');
    const scoreElement = document.getElementById('score');
    const totalElement = document.getElementById('total');
    const overlay = document.getElementById('message-overlay');

    // Game Config
    const giftImages = [
        '/images/games/santa-rescue/gift-red.png',
        '/images/games/santa-rescue/gift-green.png',
        '/images/games/santa-rescue/gift-blue.png',
        '/images/games/santa-rescue/gift-gold.png'
    ];

    // Randomly choose between 3 to 6 gifts
    const totalGifts = Math.floor(Math.random() * 4) + 3;
    let giftsCollected = 0;

    totalElement.textContent = totalGifts;

    // Sound effects (Audio API for generated beep/melodies since we don't have files yet)
    const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

    function playCollectSound() {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const oscillator = audioCtx.createOscillator();
        const gainNode = audioCtx.createGain();
        oscillator.type = 'sine';
        oscillator.frequency.setValueAtTime(500, audioCtx.currentTime);
        oscillator.frequency.exponentialRampToValueAtTime(1000, audioCtx.currentTime + 0.1);
        gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.1);
        oscillator.connect(gainNode);
        gainNode.connect(audioCtx.destination);
        oscillator.start();
        oscillator.stop(audioCtx.currentTime + 0.1);
    }

    function playWinSound() {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        // Simple "Ho Ho Ho" melody effect
        const notes = [523.25, 659.25, 783.99]; // C E G
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

    function spawnGifts() {
        for (let i = 0; i < totalGifts; i++) {
            const gift = document.createElement('img');
            gift.src = giftImages[Math.floor(Math.random() * giftImages.length)];
            gift.classList.add('gift');

            // Random position with padding
            const x = Math.random() * (window.innerWidth - 140) + 10;
            const y = Math.random() * (window.innerHeight - 300) + 100; // Keep away from top score and bottom logic

            gift.style.left = `${x}px`;
            gift.style.top = `${y}px`;

            // Random float delay
            gift.style.animationDelay = `${Math.random() * 2}s`;

            gift.addEventListener('click', (e) => collectGift(e, gift));
            gift.addEventListener('touchstart', (e) => {
                e.preventDefault(); // Prevent double firing
                collectGift(e, gift);
            });

            container.appendChild(gift);
        }
    }

    function collectGift(e, gift) {
        if (gift.classList.contains('collected')) return;

        gift.classList.add('collected');
        playCollectSound();
        createSparkle(e.clientX || e.touches[0].clientX, e.clientY || e.touches[0].clientY);

        giftsCollected++;
        scoreElement.textContent = giftsCollected;

        // Remove from DOM after animation
        setTimeout(() => {
            gift.remove();
            if (giftsCollected >= totalGifts) {
                gameWin();
            }
        }, 500);
    }

    function gameWin() {
        setTimeout(() => {
            playWinSound();
            overlay.classList.remove('hidden');
            createSnow();
        }, 300);
    }

    function createSnow() {
        setInterval(() => {
            const flake = document.createElement('div');
            flake.classList.add('snowflake');
            flake.innerHTML = '❄';
            flake.style.left = Math.random() * 100 + 'vw';
            flake.style.animationDuration = Math.random() * 3 + 2 + 's';
            flake.style.fontSize = Math.random() * 20 + 10 + 'px';
            container.appendChild(flake);

            setTimeout(() => flake.remove(), 5000);
        }, 200);
    }

    spawnGifts();
});

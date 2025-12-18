/**
 * 氣球戳戳樂 - 遊戲引擎 (高品質重構 V2)
 * 繼承自 BaseGame。
 */
class BalloonPopGame extends BaseGame {
    constructor() {
        const levels = [
            { time: 10, target: 10 },
            { time: 20, target: 20 },
            { time: 30, target: 30 },
            { time: 40, target: 40 },
            { time: 50, target: 50 }
        ];
        super('balloon-pop', levels);

        this.balloons = [];
        this.maxBalloons = 8;
        this.balloonLifetime = 1000;
        this.colors = ['red', 'blue', 'yellow', 'green', 'purple', 'pink', 'orange', 'cyan'];
        this.spawnIntervalId = null;
    }

    onInit() {
        this.balloonArea = document.getElementById('balloons-area');
    }

    onDifficultySet(difficulty) {
        this.balloonLifetime = difficulty === 'easy' ? 2000 : 1000;
    }

    onLevelStart() {
        this.startSpawning();
    }

    startSpawning() {
        if (this.spawnIntervalId) clearInterval(this.spawnIntervalId);

        const level = this.levels[this.currentLevel];
        const spawnRate = Math.max(200, (level.time * 1000) / (level.target * 1.8));

        this.spawnIntervalId = setInterval(() => {
            if (this.gameState === 'PLAYING' && this.balloons.length < this.maxBalloons) {
                this.spawnBalloon();
            }
        }, spawnRate);
    }

    spawnBalloon() {
        const balloon = document.createElement('div');
        balloon.className = 'balloon ' + this.colors[Math.floor(Math.random() * this.colors.length)];

        const x = Math.random() * (window.innerWidth - 100);
        const y = 200 + Math.random() * (window.innerHeight - 400);

        balloon.style.left = x + 'px';
        balloon.style.top = y + 'px';

        const pop = (e) => {
            e.preventDefault();
            this.handlePop(balloon);
        };

        balloon.addEventListener('mousedown', pop);
        balloon.addEventListener('touchstart', pop, { passive: false });

        this.balloonArea.appendChild(balloon);
        this.balloons.push(balloon);

        setTimeout(() => {
            if (balloon.parentNode) {
                balloon.remove();
                this.balloons = this.balloons.filter(b => b !== balloon);
            }
        }, this.balloonLifetime);
    }

    handlePop(balloon) {
        if (this.gameState !== 'PLAYING' || balloon.classList.contains('popped')) return;

        balloon.classList.add('popped');
        this.createParticles(balloon);
        GameSound.play('pop');

        this.score++;
        this.updateUI();

        if (this.score >= this.levels[this.currentLevel].target) {
            this.endLevel(true);
        }

        setTimeout(() => {
            if (balloon.parentNode) balloon.remove();
            this.balloons = this.balloons.filter(b => b !== balloon);
        }, 300);
    }

    createParticles(balloon) {
        const rect = balloon.getBoundingClientRect();
        const color = window.getComputedStyle(balloon).getPropertyValue('--balloon-color');

        for (let i = 0; i < 6; i++) {
            const particle = document.createElement('div');
            particle.className = 'pop-particle';
            particle.style.setProperty('--particle-color', color);
            particle.style.left = rect.left + rect.width / 2 + 'px';
            particle.style.top = rect.top + rect.height / 2 + 'px';

            const angle = (Math.PI * 2 * i) / 6;
            const distance = 50 + Math.random() * 30;
            particle.style.setProperty('--tx', Math.cos(angle) * distance + 'px');
            particle.style.setProperty('--ty', Math.sin(angle) * distance + 'px');

            document.body.appendChild(particle);
            setTimeout(() => particle.remove(), 600);
        }
    }

    onLevelEnd(success) {
        clearInterval(this.spawnIntervalId);
        this.balloons.forEach(b => b.remove());
        this.balloons = [];
    }

    showHint() {
        if (this.balloons.length > 0) {
            this.balloons[0].classList.add('hint-glow');
        }
    }

    hideHint() {
        this.balloons.forEach(b => b.classList.remove('hint-glow'));
    }

    playWelcomeVoice() {
        GameAudio.speak("🎈 準備好了嗎？快幫我戳破氣球吧！點點看！", "zh-TW");
    }
}

let gameInstance = null;
function startGameWithDifficulty(d) {
    if (!gameInstance) gameInstance = new BalloonPopGame();
    gameInstance.startWithDifficulty(d);
}

window.addEventListener('load', () => {
    gameInstance = new BalloonPopGame();
    gameInstance.init();
});

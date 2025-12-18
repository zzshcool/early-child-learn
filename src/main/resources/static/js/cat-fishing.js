/**
 * 小貓釣魚 - 遊戲引擎 (高品質重構 V2)
 * 繼承自 BaseGame。
 */
class CatFishingGame extends BaseGame {
    constructor() {
        const levels = [
            { time: 30, target: 10, spawnRate: 1500, fishLifetime: 3000 },
            { time: 40, target: 20, spawnRate: 1200, fishLifetime: 2500 },
            { time: 50, target: 30, spawnRate: 1000, fishLifetime: 2000 },
            { time: 60, target: 45, spawnRate: 800, fishLifetime: 1800 },
            { time: 70, target: 60, spawnRate: 600, fishLifetime: 1500 }
        ];
        super('cat-fishing', levels);

        this.fishTypes = [
            { type: 'common', emoji: '🐟', points: 1, chance: 0.5 },
            { type: 'goldfish', emoji: '🐠', points: 2, chance: 0.3 },
            { type: 'tropical', emoji: '🐡', points: 3, chance: 0.15 },
            { type: 'rare', emoji: '🦈', points: 5, chance: 0.05 }
        ];

        this.fishes = [];
        this.maxFishes = 6;
        this.spawnIntervalId = null;
    }

    onInit() {
        this.waterArea = document.getElementById('water-area');
    }

    onLevelStart() {
        this.startSpawning();
    }

    startSpawning() {
        if (this.spawnIntervalId) clearInterval(this.spawnIntervalId);

        const level = this.levels[this.currentLevel];
        this.spawnFish(); // 立即生成

        this.spawnIntervalId = setInterval(() => {
            if (this.gameState === 'PLAYING' && this.fishes.length < this.maxFishes) {
                this.spawnFish();
            }
        }, level.spawnRate);
    }

    spawnFish() {
        const fishType = this.selectFishType();
        const fishEl = document.createElement('div');
        fishEl.className = 'swimming-fish';
        fishEl.dataset.points = fishType.points;
        fishEl.textContent = fishType.emoji;

        const waterRect = this.waterArea.getBoundingClientRect();
        const fishSize = 60;
        const x = Math.random() * (waterRect.width - fishSize);
        const y = Math.random() * (waterRect.height - fishSize);

        fishEl.style.left = x + 'px';
        fishEl.style.top = y + 'px';

        const direction = Math.random() > 0.5 ? 1 : -1;
        fishEl.style.setProperty('--swim-direction', direction);
        if (direction < 0) fishEl.style.transform = 'scaleX(-1)';

        const catchHandler = (e) => {
            e.preventDefault();
            this.handleCatch(fishEl);
        };

        fishEl.addEventListener('mousedown', catchHandler);
        fishEl.addEventListener('touchstart', catchHandler, { passive: false });

        this.waterArea.appendChild(fishEl);
        this.fishes.push(fishEl);

        setTimeout(() => {
            if (fishEl.parentNode && !fishEl.classList.contains('caught')) {
                fishEl.style.animation = 'fishDisappear 0.3s ease-out forwards';
                setTimeout(() => this.removeFish(fishEl), 300);
            }
        }, this.levels[this.currentLevel].fishLifetime);
    }

    selectFishType() {
        const rand = Math.random();
        let cumulative = 0;
        for (const ft of this.fishTypes) {
            cumulative += ft.chance;
            if (rand <= cumulative) return ft;
        }
        return this.fishTypes[0];
    }

    handleCatch(fishEl) {
        if (this.gameState !== 'PLAYING' || fishEl.classList.contains('caught')) return;

        fishEl.classList.add('caught');
        const pts = parseInt(fishEl.dataset.points);
        this.score += pts;
        this.updateUI();

        this.createCatchEffect(fishEl, pts);
        GameSound.play('pop');

        if (this.score >= this.levels[this.currentLevel].target) {
            this.endLevel(true);
        }

        setTimeout(() => this.removeFish(fishEl), 300);
    }

    createCatchEffect(fishEl, points) {
        const rect = fishEl.getBoundingClientRect();
        const waterRect = this.waterArea.getBoundingClientRect();

        const p = document.createElement('div');
        p.className = 'score-popup';
        p.textContent = `+${points}`;
        p.style.left = (rect.left - waterRect.left + rect.width / 2) + 'px';
        p.style.top = (rect.top - waterRect.top) + 'px';
        this.waterArea.appendChild(p);

        for (let i = 0; i < 5; i++) {
            const s = document.createElement('div');
            s.className = 'splash';
            s.style.left = (rect.left - waterRect.left + rect.width / 2) + 'px';
            s.style.top = (rect.top - waterRect.top + rect.height / 2) + 'px';
            s.style.setProperty('--angle', (Math.random() * 360) + 'deg');
            s.style.setProperty('--distance', (30 + Math.random() * 30) + 'px');
            this.waterArea.appendChild(s);
            setTimeout(() => s.remove(), 500);
        }
        setTimeout(() => p.remove(), 800);
    }

    removeFish(fishEl) {
        if (fishEl.parentNode) fishEl.remove();
        this.fishes = this.fishes.filter(f => f !== fishEl);
    }

    onLevelEnd() {
        clearInterval(this.spawnIntervalId);
        this.fishes.forEach(f => f.remove());
        this.fishes = [];
    }

    showHint() {
        if (this.fishes.length > 0) {
            this.fishes[0].classList.add('hint-glow');
        }
    }

    hideHint() {
        this.fishes.forEach(f => f.classList.remove('hint-glow'));
    }

    playWelcomeVoice() {
        GameAudio.speak("😸 小貓咪肚子餓了，快幫他在池塘裡釣魚吧！點擊游動的魚！", "zh-TW");
    }
}

let gameInstance = null;
function startGame() {
    if (!gameInstance) {
        gameInstance = new CatFishingGame();
        gameInstance.init();
    }
    gameInstance.startWithDifficulty('normal'); // 小貓釣魚原版無難度選擇，預設普通
}

window.addEventListener('load', () => {
    gameInstance = new CatFishingGame();
    gameInstance.init();
});

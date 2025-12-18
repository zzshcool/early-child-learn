/**
 * 拯救禮物大作戰 - 遊戲引擎 (高品質重構 V3)
 * 繼承自 BaseGame 以獲得統一生命週期管理。
 */
class SantaRescueGame extends BaseGame {
    constructor() {
        const levels = [
            { time: 20, target: 20, spawnRate: 600 },
            { time: 30, target: 60, spawnRate: 500 },
            { time: 40, target: 150, spawnRate: 400 },
            { time: 50, target: 300, spawnRate: 350 },
            { time: 60, target: 500, spawnRate: 300 }
        ];
        super('santa-rescue', levels);

        this.giftTypes = [
            { type: 1, score: 1, speed: 2, img: '/images/games/santa-rescue/gift-box.png', weight: 40 },
            { type: 2, score: 2, speed: 3, img: '/images/games/santa-rescue/cookie.png', weight: 25 },
            { type: 3, score: 3, speed: 4, img: '/images/games/santa-rescue/candy.png', weight: 20 },
            { type: 4, score: 4, speed: 5, img: '/images/games/santa-rescue/chocolate.png', weight: 10 },
            { type: 5, score: 10, speed: 7, img: '/images/games/santa-rescue/reindeer.png', weight: 5 }
        ];

        this.activeGifts = [];
        this.spawnTimeout = null;
        this.lastFrameTime = 0;
        this.totalStarsAdded = 0;
    }

    onInit() {
        this.container = document.getElementById('game-container');
        this.startSnow();
        window.addEventListener('resize', () => this.handleResize());
    }

    onDifficultySet(difficulty) {
        if (difficulty === 'easy') {
            this.giftTypes.forEach(t => t.speed *= 0.8);
        } else if (difficulty === 'hard') {
            this.giftTypes.forEach(t => t.speed *= 1.2);
        }
    }

    startSnow() {
        setInterval(() => this.createSnowflake(), 300);
    }

    createSnowflake() {
        if (document.hidden || this.gameState === 'IDLE') return;
        const snowflake = document.createElement('div');
        snowflake.classList.add('snowflake');
        snowflake.innerHTML = '❄';
        snowflake.style.left = Math.random() * 100 + 'vw';
        snowflake.style.animationDuration = Math.random() * 3 + 2 + 's';
        this.container.appendChild(snowflake);
        setTimeout(() => snowflake.remove(), 5000);
    }

    onLevelStart() {
        this.loop(performance.now());
        this.scheduleSpawn();
    }

    scheduleSpawn() {
        if (this.gameState !== 'PLAYING') return;
        const level = this.levels[this.currentLevel];
        this.spawnTimeout = setTimeout(() => {
            this.spawnGift();
            this.scheduleSpawn();
        }, level.spawnRate + Math.random() * 500);
    }

    spawnGift() {
        let rand = Math.random() * 100;
        let cumulative = 0;
        let selectedType = this.giftTypes[0];
        for (const t of this.giftTypes) {
            cumulative += t.weight;
            if (rand <= cumulative) {
                selectedType = t;
                break;
            }
        }

        const giftEl = document.createElement('img');
        giftEl.src = selectedType.img;
        giftEl.classList.add('gift', `gift-type-${selectedType.type}`);

        const padding = 60;
        const x = padding + Math.random() * (window.innerWidth - padding * 2 - 100);
        giftEl.style.left = x + 'px';
        giftEl.style.top = '-120px';

        const giftObj = {
            el: giftEl,
            type: selectedType,
            y: -120,
            speed: selectedType.speed + (Math.random() * 1),
            collected: false
        };

        const collect = (e) => {
            e.preventDefault();
            this.handleCollect(giftObj, e);
        };

        giftEl.addEventListener('mousedown', collect);
        giftEl.addEventListener('touchstart', collect, { passive: false });

        this.container.appendChild(giftEl);
        this.activeGifts.push(giftObj);
    }

    handleCollect(gift, e) {
        if (this.gameState !== 'PLAYING' || gift.collected) return;
        gift.collected = true;
        this.score += gift.type.score;
        this.updateUI();

        gift.el.classList.add('collected');
        GameSound.play('pop');

        let cx = e.clientX || (e.touches ? e.touches[0].clientX : 0);
        let cy = e.clientY || (e.touches ? e.touches[0].clientY : 0);
        this.showPopup(cx, cy, `+${gift.type.score}`);

        const newStars = Math.floor(this.score / 100);
        if (newStars > this.totalStarsAdded) {
            RewardSystem.addStars(newStars - this.totalStarsAdded);
            this.totalStarsAdded = newStars;
        }

        setTimeout(() => this.removeGift(gift), 300);

        if (this.score >= this.levels[this.currentLevel].target) {
            this.endLevel(true);
        }
    }

    showPopup(x, y, text) {
        const p = document.createElement('div');
        p.className = 'score-popup';
        p.textContent = text;
        p.style.left = x + 'px';
        p.style.top = y + 'px';
        this.container.appendChild(p);
        setTimeout(() => p.remove(), 800);
    }

    removeGift(gift) {
        if (gift.el && gift.el.parentNode) gift.el.remove();
        this.activeGifts = this.activeGifts.filter(g => g !== gift);
    }

    loop(time) {
        if (this.gameState !== 'PLAYING') return;
        requestAnimationFrame((t) => this.loop(t));

        const deltaTime = (time - this.lastFrameTime) / 16;
        this.lastFrameTime = time;

        for (let i = this.activeGifts.length - 1; i >= 0; i--) {
            const g = this.activeGifts[i];
            if (g.collected) continue;

            g.y += g.speed * deltaTime;
            g.el.style.top = g.y + 'px';
            g.el.style.transform = `rotate(${Math.sin(time / 200) * 5}deg)`;

            if (g.y > window.innerHeight + 100) {
                this.removeGift(g);
            }
        }
    }

    onLevelEnd(success) {
        clearTimeout(this.spawnTimeout);
        this.activeGifts.forEach(g => g.el.remove());
        this.activeGifts = [];
    }

    showHint() {
        if (this.activeGifts.length > 0) {
            const lowest = this.activeGifts.sort((a, b) => b.y - a.y)[0];
            if (lowest && !lowest.collected) {
                lowest.el.classList.add('hint-glow');
            }
        }
    }

    hideHint() {
        this.activeGifts.forEach(g => g.el.classList.remove('hint-glow'));
    }

    playWelcomeVoice() {
        GameAudio.speak("🎅 聖誕節快到了，快幫我收集掉落的禮物吧！點擊它們！", "zh-TW");
    }

    handleResize() { }
}

let gameInstance = null;
function startGameWithDifficulty(d) {
    if (!gameInstance) gameInstance = new SantaRescueGame();
    gameInstance.startWithDifficulty(d);
}

window.addEventListener('load', () => {
    gameInstance = new SantaRescueGame();
    gameInstance.init();
});

/**
 * 拯救禮物大作戰 - 遊戲引擎 (V2 落下式)
 */
class SantaRescueGame {
    constructor() {
        this.levels = [
            { time: 20, target: 50, spawnRate: 1000 },
            { time: 30, target: 120, spawnRate: 800 },
            { time: 40, target: 250, spawnRate: 600 },
            { time: 50, target: 400, spawnRate: 500 },
            { time: 60, target: 600, spawnRate: 400 }
        ];

        this.giftTypes = [
            { type: 1, score: 1, speed: 2, img: '/images/games/santa-rescue/gift-box.png', weight: 50 },
            { type: 2, score: 2, speed: 3, img: '/images/games/santa-rescue/cookie.png', weight: 25 },
            { type: 3, score: 3, speed: 4, img: '/images/games/santa-rescue/candy.png', weight: 15 },
            { type: 4, score: 4, speed: 5, img: '/images/games/santa-rescue/chocolate.png', weight: 8 },
            { type: 5, score: 10, speed: 7, img: '/images/games/santa-rescue/reindeer.png', weight: 2 }
        ];

        this.currentLevel = 0;
        this.score = 0;
        this.totalStarsAdded = 0;
        this.timeLeft = 0;
        this.activeGifts = [];
        this.gameActive = false;
        this.timerInterval = null;
        this.spawnTimeout = null;
        this.lastFrameTime = 0;
    }

    init() {
        this.container = document.getElementById('game-container');
        this.scoreElement = document.getElementById('score');
        this.timeElement = document.getElementById('time');
        this.levelElement = document.getElementById('current-level');
        this.targetElement = document.getElementById('target-score');
        this.progressFill = document.querySelector('.progress-fill');

        // 初始化背景雪花
        this.startSnow();

        // 監聽視窗縮放
        window.addEventListener('resize', () => this.handleResize());
    }

    startSnow() {
        setInterval(() => this.createSnowflake(), 300);
    }

    createSnowflake() {
        if (document.hidden) return;
        const snowflake = document.createElement('div');
        snowflake.classList.add('snowflake');
        snowflake.innerHTML = '❄';
        snowflake.style.left = Math.random() * 100 + 'vw';
        snowflake.style.animationDuration = Math.random() * 3 + 2 + 's';
        snowflake.style.fontSize = Math.random() * 15 + 10 + 'px';
        snowflake.style.opacity = Math.random() * 0.5 + 0.3;
        this.container.appendChild(snowflake);
        setTimeout(() => snowflake.remove(), 5000);
    }

    startWithDifficulty(difficulty) {
        document.getElementById('difficulty-overlay').classList.add('hidden');
        document.getElementById('game-ui').classList.remove('hidden');

        // 難度調整邏輯 (V2 簡化)
        this.difficulty = difficulty;
        if (difficulty === 'easy') {
            this.giftTypes.forEach(t => t.speed *= 0.8);
        } else if (difficulty === 'hard') {
            this.giftTypes.forEach(t => t.speed *= 1.2);
        }

        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.createStarDisplay();
        }

        this.startLevel(0);
    }

    startLevel(levelIndex) {
        this.currentLevel = levelIndex;
        this.score = 0;
        const level = this.levels[levelIndex];
        this.timeLeft = level.time;
        this.gameActive = false;

        this.showLevelTransition(levelIndex, () => {
            this.gameActive = true;
            this.updateUI();
            this.startTimer();
            this.loop(performance.now());
            this.scheduleSpawn();
        });
    }

    showLevelTransition(levelIndex, callback) {
        const overlay = document.getElementById('level-transition');
        const level = this.levels[levelIndex];
        document.getElementById('transition-level-num').textContent = levelIndex + 1;
        document.getElementById('transition-time').textContent = level.time;
        document.getElementById('transition-target').textContent = level.target;
        overlay.classList.remove('hidden');

        let count = 3;
        const countEl = document.getElementById('countdown');
        countEl.textContent = count;

        const countInt = setInterval(() => {
            count--;
            if (count > 0) {
                countEl.textContent = count;
                GameSound.play('click');
            } else {
                countEl.textContent = 'GO!';
                GameSound.play('correct');
                clearInterval(countInt);
                setTimeout(() => {
                    overlay.classList.add('hidden');
                    callback();
                }, 500);
            }
        }, 1000);
    }

    startTimer() {
        this.timerInterval = setInterval(() => {
            this.timeLeft--;
            this.timeElement.textContent = this.timeLeft;
            if (this.timeLeft <= 0) this.endLevel(false);
        }, 1000);
    }

    scheduleSpawn() {
        if (!this.gameActive) return;
        const level = this.levels[this.currentLevel];
        this.spawnTimeout = setTimeout(() => {
            this.spawnGift();
            this.scheduleSpawn();
        }, level.spawnRate + Math.random() * 500);
    }

    spawnGift() {
        // 加權隨機選擇禮物類型
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

        // 隨機橫向位置
        const padding = 50;
        const x = padding + Math.random() * (window.innerWidth - padding * 2 - 100);
        giftEl.style.left = x + 'px';
        giftEl.style.top = '-120px'; // 從上方掉下

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
        if (!this.gameActive || gift.collected) return;
        gift.collected = true;
        this.score += gift.type.score;
        this.updateUI();

        // 視覺與音效回饋
        gift.el.classList.add('collected');
        GameSound.play('pop');

        let cx = e.clientX || (e.touches ? e.touches[0].clientX : 0);
        let cy = e.clientY || (e.touches ? e.touches[0].clientY : 0);
        this.showPopup(cx, cy, `+${gift.type.score}`);

        // 檢查星星獎勵 (每100分得1星)
        const newStars = Math.floor(this.score / 100);
        if (newStars > this.totalStarsAdded) {
            const diff = newStars - this.totalStarsAdded;
            RewardSystem.addStars(diff);
            this.totalStarsAdded = newStars;
        }

        setTimeout(() => this.removeGift(gift), 300);
        this.checkLevelComplete();
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
        if (!this.gameActive) return;
        requestAnimationFrame((t) => this.loop(t));

        const deltaTime = (time - this.lastFrameTime) / 16; // 基準為 60fps
        this.lastFrameTime = time;

        const screenHeight = window.innerHeight;

        for (let i = this.activeGifts.length - 1; i >= 0; i--) {
            const g = this.activeGifts[i];
            if (g.collected) continue;

            g.y += g.speed * deltaTime;
            g.el.style.top = g.y + 'px';

            // 旋轉微動
            g.el.style.transform = `rotate(${Math.sin(time / 200) * 5}deg)`;

            // 沒點到掉出螢幕
            if (g.y > screenHeight + 100) {
                this.removeGift(g);
            }
        }
    }

    checkLevelComplete() {
        const target = this.levels[this.currentLevel].target;
        if (this.score >= target) {
            this.endLevel(true);
        }
    }

    endLevel(success) {
        this.gameActive = false;
        clearInterval(this.timerInterval);
        clearTimeout(this.spawnTimeout);

        this.activeGifts.forEach(g => g.el.remove());
        this.activeGifts = [];

        if (success) {
            const isLast = this.currentLevel === this.levels.length - 1;
            this.showResult(true, isLast ? 'all_completed' : 'level_completed');
        } else {
            this.showResult(false, 'timeout');
        }
    }

    showResult(won, type) {
        const overlay = document.getElementById('message-overlay');
        const msg = document.getElementById('message-text');
        overlay.classList.remove('hidden');

        if (won) {
            GameAudio.correct();
            GameSound.play('win');
            if (type === 'all_completed') {
                msg.textContent = '🎄 恭喜！聖誕任務圓滿達成！ 🎁';
                RewardSystem.recordGameComplete('santa-rescue');
            } else {
                msg.textContent = `第 ${this.currentLevel + 1} 關成功！`;
                setTimeout(() => {
                    overlay.classList.add('hidden');
                    this.startLevel(this.currentLevel + 1);
                }, 2000);
            }
        } else {
            GameAudio.tryAgain();
            GameSound.play('wrong');
            msg.textContent = `時間到了！得到 ${this.score} 分`;
        }
    }

    updateUI() {
        this.scoreElement.textContent = this.score;
        this.targetElement.textContent = this.levels[this.currentLevel].target;
        this.levelElement.textContent = this.currentLevel + 1;
        const progress = Math.min(100, (this.score / this.levels[this.currentLevel].target) * 100);
        this.progressFill.style.width = progress + '%';
    }

    handleResize() {
        // 可擴展自適應邏輯
    }
}

let gameInstance = null;

function startGameWithDifficulty(d) {
    if (!gameInstance) {
        gameInstance = new SantaRescueGame();
        gameInstance.init();
    }
    gameInstance.startWithDifficulty(d);
}

window.addEventListener('load', () => {
    gameInstance = new SantaRescueGame();
    gameInstance.init();
});

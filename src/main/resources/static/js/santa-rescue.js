/**
 * 拯救禮物大作戰 - 遊戲引擎
 * 點擊禮物收集分數！
 */
class SantaRescueGame {
    constructor() {
        this.levels = [
            { time: 20, target: 10, spawnRate: 900, giftLifetime: 2000 },
            { time: 30, target: 20, spawnRate: 800, giftLifetime: 1800 },
            { time: 40, target: 35, spawnRate: 700, giftLifetime: 1500 },
            { time: 50, target: 50, spawnRate: 600, giftLifetime: 1200 },
            { time: 60, target: 70, spawnRate: 500, giftLifetime: 1000 }
        ];

        this.giftImages = [
            '/images/games/santa-rescue/gift-red.png',
            '/images/games/santa-rescue/gift-green.png',
            '/images/games/santa-rescue/gift-blue.png',
            '/images/games/santa-rescue/gift-gold.png'
        ];

        this.currentLevel = 0;
        this.score = 0;
        this.combo = 0;
        this.maxCombo = 0;
        this.timeLeft = 0;
        this.gifts = [];
        this.maxGifts = 8;
        this.difficulty = 'normal';
        this.timerInterval = null;
        this.spawnInterval = null;
        this.snowInterval = null;
        this.gameActive = false;
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }

    init() {
        this.container = document.getElementById('game-container');
        this.scoreElement = document.getElementById('score');
        this.timeElement = document.getElementById('time');
        this.levelElement = document.getElementById('current-level');
        this.targetElement = document.getElementById('target-score');
        this.comboElement = document.getElementById('combo-display');
        this.progressFill = document.querySelector('.progress-fill');

        // 開始雪花效果
        this.startSnow();
    }

    startSnow() {
        this.snowInterval = setInterval(() => this.createSnowflake(), 200);
    }

    createSnowflake() {
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
        this.difficulty = difficulty;

        // 根據難度調整參數
        if (difficulty === 'easy') {
            this.levels.forEach(level => {
                level.giftLifetime = level.giftLifetime * 1.5;
                level.spawnRate = level.spawnRate * 1.2;
            });
            this.maxGifts = 10;
        } else if (difficulty === 'hard') {
            this.levels.forEach(level => {
                level.giftLifetime = level.giftLifetime * 0.7;
                level.spawnRate = level.spawnRate * 0.8;
            });
            this.maxGifts = 6;
        }

        document.getElementById('difficulty-overlay').classList.add('hidden');
        document.getElementById('game-ui').classList.remove('hidden');

        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }

        this.startLevel(0);
    }

    startLevel(levelIndex) {
        this.currentLevel = levelIndex;
        this.score = 0;
        this.combo = 0;
        this.maxCombo = 0;
        const level = this.levels[levelIndex];
        this.timeLeft = level.time;
        this.gameActive = false;

        this.showLevelTransition(levelIndex, () => {
            this.gameActive = true;
            this.updateUI();
            this.startTimer();
            this.startSpawning();
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
        const countdownEl = document.getElementById('countdown');
        countdownEl.textContent = count;
        this.playBeep(500);

        const countInterval = setInterval(() => {
            count--;
            if (count > 0) {
                countdownEl.textContent = count;
                this.playBeep(500);
            } else {
                countdownEl.textContent = 'GO!';
                this.playBeep(800);
                clearInterval(countInterval);
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
            this.updateTimer();

            if (this.timeLeft <= 0) {
                this.endLevel(false);
            }
        }, 1000);
    }

    updateTimer() {
        if (this.timeElement) {
            this.timeElement.textContent = this.timeLeft;
        }

        const timerDisplay = document.getElementById('timer-display');
        if (timerDisplay) {
            if (this.timeLeft <= 5) {
                timerDisplay.classList.add('warning');
            } else {
                timerDisplay.classList.remove('warning');
            }
        }
    }

    startSpawning() {
        const level = this.levels[this.currentLevel];

        // 立即生成幾個禮物
        for (let i = 0; i < 3; i++) {
            setTimeout(() => this.spawnGift(), i * 200);
        }

        this.spawnInterval = setInterval(() => {
            if (this.gifts.length < this.maxGifts && this.gameActive) {
                this.spawnGift();
            }
        }, level.spawnRate);
    }

    spawnGift() {
        if (!this.gameActive) return;

        const gift = document.createElement('img');
        gift.src = this.giftImages[Math.floor(Math.random() * this.giftImages.length)];
        gift.classList.add('gift');

        // 隨機位置
        const x = 20 + Math.random() * (window.innerWidth - 140);
        const y = 150 + Math.random() * (window.innerHeight - 350);

        gift.style.left = x + 'px';
        gift.style.top = y + 'px';
        gift.style.transform = 'scale(0)';

        // 點擊事件
        const collectHandler = (e) => {
            e.preventDefault();
            if (this.gameActive && !gift.classList.contains('collected')) {
                this.collectGift(e, gift);
            }
        };

        gift.addEventListener('click', collectHandler);
        gift.addEventListener('touchstart', collectHandler, { passive: false });

        this.container.appendChild(gift);
        this.gifts.push(gift);

        // 彈出動畫
        requestAnimationFrame(() => {
            gift.style.transform = 'scale(1)';
        });

        // 自動消失
        const level = this.levels[this.currentLevel];
        const removeTimer = setTimeout(() => {
            if (gift.parentNode && !gift.classList.contains('collected')) {
                this.combo = 0; // 錯過禮物，連擊歸零
                this.updateCombo();
                gift.style.transform = 'scale(0)';
                setTimeout(() => this.removeGift(gift), 200);
            }
        }, level.giftLifetime);

        gift.dataset.timerId = removeTimer;
    }

    collectGift(e, gift) {
        clearTimeout(parseInt(gift.dataset.timerId));
        gift.classList.add('collected');

        // 連擊加成
        this.combo++;
        if (this.combo > this.maxCombo) {
            this.maxCombo = this.combo;
        }

        const comboBonus = Math.min(Math.floor(this.combo / 5), 3); // 最多+3分
        const points = 1 + comboBonus;

        this.score += points;
        this.updateUI();
        this.updateCombo();

        // 特效
        let cx = e.clientX;
        let cy = e.clientY;
        if (!cx && e.touches && e.touches.length > 0) {
            cx = e.touches[0].clientX;
            cy = e.touches[0].clientY;
        }
        this.createSparkle(cx, cy);
        this.showScorePopup(cx, cy, points);
        this.playCollect();

        // 移除禮物
        setTimeout(() => this.removeGift(gift), 300);

        // 檢查過關
        this.checkLevelComplete();
    }

    updateCombo() {
        if (this.comboElement) {
            if (this.combo >= 5) {
                this.comboElement.classList.remove('hidden');
                this.comboElement.textContent = `🔥 ${this.combo} 連擊！`;
                this.comboElement.style.animation = 'none';
                this.comboElement.offsetHeight; // 觸發 reflow
                this.comboElement.style.animation = 'comboPopup 0.3s ease-out';
            } else {
                this.comboElement.classList.add('hidden');
            }
        }
    }

    showScorePopup(x, y, points) {
        const popup = document.createElement('div');
        popup.className = 'score-popup';
        popup.textContent = `+${points}`;
        popup.style.left = x + 'px';
        popup.style.top = y + 'px';
        this.container.appendChild(popup);
        setTimeout(() => popup.remove(), 800);
    }

    createSparkle(x, y) {
        for (let i = 0; i < 6; i++) {
            const sparkle = document.createElement('div');
            sparkle.classList.add('sparkle');
            sparkle.style.left = (x + (Math.random() - 0.5) * 60) + 'px';
            sparkle.style.top = (y + (Math.random() - 0.5) * 60) + 'px';
            this.container.appendChild(sparkle);
            setTimeout(() => sparkle.remove(), 600);
        }
    }

    removeGift(gift) {
        if (gift.parentNode) {
            gift.remove();
        }
        this.gifts = this.gifts.filter(g => g !== gift);
    }

    checkLevelComplete() {
        const level = this.levels[this.currentLevel];
        if (this.score >= level.target) {
            this.endLevel(true);
        }
    }

    endLevel(success) {
        this.gameActive = false;
        clearInterval(this.timerInterval);
        clearInterval(this.spawnInterval);

        this.gifts.forEach(gift => gift.remove());
        this.gifts = [];

        if (success) {
            this.playLevelComplete();

            if (this.currentLevel < this.levels.length - 1) {
                this.showLevelCompleteMessage(() => {
                    this.startLevel(this.currentLevel + 1);
                });
            } else {
                this.showResult(true, 'complete');
            }
        } else {
            this.playFail();
            this.showResult(false, 'timeout');
        }
    }

    showLevelCompleteMessage(callback) {
        const overlay = document.getElementById('message-overlay');
        const messageText = document.getElementById('message-text');
        const bubbleText = document.querySelector('#bubble');
        const buttons = document.querySelector('.result-buttons');

        messageText.textContent = '過關了！🎉';
        if (bubbleText) bubbleText.textContent = 'Ho Ho Ho!';
        if (buttons) buttons.style.display = 'none';

        overlay.classList.remove('hidden');

        setTimeout(() => {
            overlay.classList.add('hidden');
            if (buttons) buttons.style.display = 'flex';
            callback();
        }, 1500);
    }

    showResult(won, reason) {
        const overlay = document.getElementById('message-overlay');
        const messageText = document.getElementById('message-text');
        const bubbleText = document.querySelector('#bubble');
        const buttons = document.querySelector('.result-buttons');

        if (buttons) buttons.style.display = 'flex';

        if (won && reason === 'complete') {
            messageText.textContent = `🎄 聖誕快樂！全部通關！🎄`;
            if (bubbleText) bubbleText.textContent = `最高連擊: ${this.maxCombo}!`;
            this.playCelebration();
        } else if (!won && reason === 'timeout') {
            const level = this.levels[this.currentLevel];
            messageText.textContent = `第 ${this.currentLevel + 1} 關時間到！`;
            if (bubbleText) bubbleText.textContent = `得到 ${this.score}/${level.target} 分`;
        }

        overlay.classList.remove('hidden');
    }

    updateUI() {
        if (this.scoreElement) {
            this.scoreElement.textContent = this.score;
        }
        if (this.targetElement) {
            this.targetElement.textContent = this.levels[this.currentLevel].target;
        }
        if (this.levelElement) {
            this.levelElement.textContent = this.currentLevel + 1;
        }

        // 進度條
        const level = this.levels[this.currentLevel];
        const progress = Math.min(100, (this.score / level.target) * 100);
        if (this.progressFill) {
            this.progressFill.style.width = progress + '%';
        }
    }

    // 音效系統
    playBeep(freq) {
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0.15, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.15);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.15);
    }

    playCollect() {
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.type = 'sine';
        osc.frequency.setValueAtTime(600 + this.combo * 20, this.audioCtx.currentTime);
        osc.frequency.exponentialRampToValueAtTime(1200 + this.combo * 30, this.audioCtx.currentTime + 0.1);
        gain.gain.setValueAtTime(0.15, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.1);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.1);
    }

    playLevelComplete() {
        const notes = [523, 659, 784];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playBeep(freq), i * 150);
        });
    }

    playCelebration() {
        const notes = [523, 587, 659, 698, 784, 880, 988, 1047];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playBeep(freq), i * 100);
        });
    }

    playFail() {
        const notes = [400, 350, 300];
        notes.forEach((freq, i) => {
            setTimeout(() => {
                const osc = this.audioCtx.createOscillator();
                const gain = this.audioCtx.createGain();
                osc.connect(gain);
                gain.connect(this.audioCtx.destination);

                osc.frequency.value = freq;
                gain.gain.setValueAtTime(0.15, this.audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.3);

                osc.start();
                osc.stop(this.audioCtx.currentTime + 0.3);
            }, i * 200);
        });
    }
}

// 全域遊戲實例
let game = null;

function startGameWithDifficulty(difficulty) {
    if (!game) {
        game = new SantaRescueGame();
        game.init();
    }
    game.startWithDifficulty(difficulty);
}

window.addEventListener('load', () => {
    game = new SantaRescueGame();
    game.init();
});

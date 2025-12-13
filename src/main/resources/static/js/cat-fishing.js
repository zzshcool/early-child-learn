/**
 * 小貓釣魚 - 遊戲引擎
 * 點擊水中游動的魚來獲得分數！
 */
class CatFishingGame {
    constructor() {
        this.levels = [
            { time: 30, target: 10, spawnRate: 1500, fishLifetime: 3000 },
            { time: 40, target: 20, spawnRate: 1200, fishLifetime: 2500 },
            { time: 50, target: 30, spawnRate: 1000, fishLifetime: 2000 },
            { time: 60, target: 45, spawnRate: 800, fishLifetime: 1800 },
            { time: 70, target: 60, spawnRate: 600, fishLifetime: 1500 }
        ];

        this.fishTypes = [
            { type: 'common', emoji: '🐟', points: 1, chance: 0.5 },
            { type: 'goldfish', emoji: '🐠', points: 2, chance: 0.3 },
            { type: 'tropical', emoji: '🐡', points: 3, chance: 0.15 },
            { type: 'rare', emoji: '🦈', points: 5, chance: 0.05 }
        ];

        this.currentLevel = 0;
        this.score = 0;
        this.timeLeft = 0;
        this.fishes = [];
        this.maxFishes = 6;
        this.timerInterval = null;
        this.spawnInterval = null;
        this.gameActive = false;
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }

    init() {
        this.cacheElements();
        this.bindEvents();
    }

    cacheElements() {
        this.container = document.getElementById('game-container');
        this.waterArea = document.getElementById('water-area');
        this.scoreElement = document.getElementById('score');
        this.targetElement = document.getElementById('target-score');
        this.timerElement = document.getElementById('time-left');
        this.levelElement = document.getElementById('current-level');
        this.progressFill = document.querySelector('.progress-fill');
        this.tutorialOverlay = document.getElementById('tutorial-overlay');
        this.levelTransition = document.getElementById('level-transition');
        this.resultOverlay = document.getElementById('result-overlay');
        this.gameUI = document.getElementById('game-ui');
    }

    bindEvents() {
        // 防止在水區域外意外點擊
        if (this.waterArea) {
            this.waterArea.addEventListener('click', (e) => {
                // 只處理點擊到魚的情況，點擊水面不做任何事
            });
        }
    }

    startGame() {
        if (this.tutorialOverlay) {
            this.tutorialOverlay.classList.add('hidden');
        }
        if (this.gameUI) {
            this.gameUI.classList.remove('hidden');
        }
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }

        // 語音提示
        if (window.SpeechHelper) {
            SpeechHelper.speak('點擊水中的魚幫小貓捕魚！');
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
            this.startSpawning();
        });
    }

    showLevelTransition(levelIndex, callback) {
        const level = this.levels[levelIndex];

        document.getElementById('transition-level-num').textContent = levelIndex + 1;
        document.getElementById('transition-time').textContent = level.time;
        document.getElementById('transition-target').textContent = level.target;

        this.levelTransition.classList.remove('hidden');

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
                countdownEl.textContent = '開始!';
                this.playBeep(800);
                clearInterval(countInterval);
                setTimeout(() => {
                    this.levelTransition.classList.add('hidden');
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
        if (this.timerElement) {
            this.timerElement.textContent = this.timeLeft;
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

        // 立即生成一條魚
        this.spawnFish();

        this.spawnInterval = setInterval(() => {
            if (this.fishes.length < this.maxFishes && this.gameActive) {
                this.spawnFish();
            }
        }, level.spawnRate);
    }

    spawnFish() {
        if (!this.gameActive || !this.waterArea) return;

        const fishType = this.selectFishType();
        const fish = document.createElement('div');
        fish.className = 'swimming-fish';
        fish.dataset.points = fishType.points;
        fish.dataset.type = fishType.type;
        fish.textContent = fishType.emoji;

        // 隨機位置（在水區域內）
        const waterRect = this.waterArea.getBoundingClientRect();
        const fishSize = 60;
        const x = Math.random() * (waterRect.width - fishSize);
        const y = Math.random() * (waterRect.height - fishSize);

        fish.style.left = x + 'px';
        fish.style.top = y + 'px';

        // 隨機游動方向
        const direction = Math.random() > 0.5 ? 1 : -1;
        fish.style.setProperty('--swim-direction', direction);
        if (direction < 0) {
            fish.style.transform = 'scaleX(-1)';
        }

        // 點擊事件
        const catchHandler = (e) => {
            e.preventDefault();
            e.stopPropagation();
            if (this.gameActive && !fish.classList.contains('caught')) {
                this.catchFish(fish);
            }
        };

        fish.addEventListener('click', catchHandler);
        fish.addEventListener('touchstart', catchHandler, { passive: false });

        this.waterArea.appendChild(fish);
        this.fishes.push(fish);

        // 入場動畫
        fish.style.animation = 'fishAppear 0.3s ease-out, fishSwim 2s ease-in-out infinite';

        // 自動消失
        const level = this.levels[this.currentLevel];
        setTimeout(() => {
            if (fish.parentNode && !fish.classList.contains('caught')) {
                fish.style.animation = 'fishDisappear 0.3s ease-out forwards';
                setTimeout(() => {
                    this.removeFish(fish);
                }, 300);
            }
        }, level.fishLifetime);
    }

    selectFishType() {
        const rand = Math.random();
        let cumulative = 0;

        for (const fishType of this.fishTypes) {
            cumulative += fishType.chance;
            if (rand <= cumulative) {
                return fishType;
            }
        }

        return this.fishTypes[0];
    }

    catchFish(fish) {
        fish.classList.add('caught');
        const points = parseInt(fish.dataset.points) || 1;

        this.score += points;
        this.updateUI();
        this.createCatchEffect(fish, points);
        this.playCatch();

        // 移除魚
        setTimeout(() => {
            this.removeFish(fish);
        }, 300);

        // 檢查是否過關
        this.checkLevelComplete();
    }

    createCatchEffect(fish, points) {
        const rect = fish.getBoundingClientRect();
        const waterRect = this.waterArea.getBoundingClientRect();

        // 分數飄浮效果
        const scorePopup = document.createElement('div');
        scorePopup.className = 'score-popup';
        scorePopup.textContent = `+${points}`;
        scorePopup.style.left = (rect.left - waterRect.left + rect.width / 2) + 'px';
        scorePopup.style.top = (rect.top - waterRect.top) + 'px';

        this.waterArea.appendChild(scorePopup);

        // 水花效果
        for (let i = 0; i < 5; i++) {
            const splash = document.createElement('div');
            splash.className = 'splash';
            splash.style.left = (rect.left - waterRect.left + rect.width / 2) + 'px';
            splash.style.top = (rect.top - waterRect.top + rect.height / 2) + 'px';
            splash.style.setProperty('--angle', (Math.random() * 360) + 'deg');
            splash.style.setProperty('--distance', (30 + Math.random() * 30) + 'px');
            this.waterArea.appendChild(splash);

            setTimeout(() => splash.remove(), 500);
        }

        setTimeout(() => scorePopup.remove(), 800);
    }

    removeFish(fish) {
        if (fish.parentNode) {
            fish.remove();
        }
        this.fishes = this.fishes.filter(f => f !== fish);
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

        // 清除所有魚
        this.fishes.forEach(fish => fish.remove());
        this.fishes = [];

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
        const icon = document.getElementById('result-icon');
        const title = document.getElementById('result-title');
        const message = document.getElementById('result-message');
        const buttons = document.querySelector('.result-buttons');

        icon.textContent = '🎉';
        title.textContent = '過關了！';
        message.textContent = '準備進入下一關...';
        buttons.style.display = 'none';

        this.resultOverlay.classList.remove('hidden');

        setTimeout(() => {
            this.resultOverlay.classList.add('hidden');
            buttons.style.display = 'flex';
            callback();
        }, 1500);
    }

    showResult(won, reason) {
        const icon = document.getElementById('result-icon');
        const title = document.getElementById('result-title');
        const message = document.getElementById('result-message');
        const buttons = document.querySelector('.result-buttons');

        buttons.style.display = 'flex';

        if (won && reason === 'complete') {
            icon.textContent = '🏆';
            title.textContent = '太棒了！全部通關！';
            message.textContent = `你成功完成了所有 ${this.levels.length} 個關卡！🎊`;
            this.playCelebration();

            // 語音回饋
            if (window.SpeechHelper) {
                SpeechHelper.speak('太棒了！你全部通關了！');
            }
        } else if (!won && reason === 'timeout') {
            const level = this.levels[this.currentLevel];
            icon.textContent = '⏰';
            title.textContent = `第 ${this.currentLevel + 1} 關時間到了！`;
            message.textContent = `得到 ${this.score}/${level.target} 分，還差 ${level.target - this.score} 分！`;

            // 語音鼓勵
            if (window.SpeechHelper) {
                SpeechHelper.speak('時間到了！沒關係，再試一次！');
            }
        }

        this.resultOverlay.classList.remove('hidden');
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

    playCatch() {
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.type = 'sine';
        osc.frequency.setValueAtTime(600, this.audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(900, this.audioCtx.currentTime + 0.1);
        gain.gain.setValueAtTime(0.2, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.2);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.2);
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

function startGame() {
    if (!game) {
        game = new CatFishingGame();
        game.init();
    }
    game.startGame();
}

window.addEventListener('load', () => {
    game = new CatFishingGame();
    game.init();
});

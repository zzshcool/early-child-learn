// 氣球戳戳樂 - 五關卡遊戲引擎
class BalloonPopGame {
    constructor() {
        this.levels = [
            { time: 10, target: 10 },
            { time: 20, target: 20 },
            { time: 30, target: 30 },
            { time: 40, target: 40 },
            { time: 50, target: 50 }
        ];
        this.currentLevel = 0;
        this.score = 0;
        this.timeLeft = 0;
        this.balloons = [];
        this.maxBalloons = 8;
        this.balloonLifetime = 1000;
        this.difficulty = 'normal';
        this.timerInterval = null;
        this.spawnInterval = null;
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        this.colors = ['red', 'blue', 'yellow', 'green', 'purple', 'pink', 'orange', 'cyan'];
    }

    startGameWithDifficulty(difficulty) {
        this.difficulty = difficulty;
        this.balloonLifetime = difficulty === 'easy' ? 2000 : 1000;
        document.getElementById('difficulty-overlay').classList.add('hidden');
        document.getElementById('game-ui').classList.remove('hidden');
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        this.startLevel(0);
    }

    startLevel(levelIndex) {
        this.currentLevel = levelIndex;
        this.score = 0;
        const level = this.levels[levelIndex];
        this.timeLeft = level.time;

        this.showLevelTransition(levelIndex, () => {
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

        const countInterval = setInterval(() => {
            count--;
            if (count > 0) {
                countdownEl.textContent = count;
                this.playBeep(600);
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

        this.playBeep(600);
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
        const timerEl = document.getElementById('time-left');
        const timerDisplay = document.getElementById('timer-display');
        timerEl.textContent = this.timeLeft;

        if (this.timeLeft <= 5) {
            timerDisplay.classList.add('warning');
        } else {
            timerDisplay.classList.remove('warning');
        }
    }

    startSpawning() {
        const level = this.levels[this.currentLevel];
        const spawnRate = this.calculateSpawnRate(level.target, level.time);

        this.spawnInterval = setInterval(() => {
            if (this.balloons.length < this.maxBalloons) {
                this.spawnBalloon();
            }
            this.cleanupBalloons();
        }, spawnRate);
    }

    calculateSpawnRate(target, time) {
        const totalNeeded = target * 1.8;
        return Math.max(200, (time * 1000) / totalNeeded);
    }

    spawnBalloon() {
        const balloon = document.createElement('div');
        balloon.className = 'balloon ' + this.colors[Math.floor(Math.random() * this.colors.length)];

        const x = Math.random() * (window.innerWidth - 100);
        const y = 200 + Math.random() * (window.innerHeight - 400);

        balloon.style.left = x + 'px';
        balloon.style.top = y + 'px';

        balloon.spawnTime = Date.now();
        balloon.addEventListener('click', () => this.popBalloon(balloon));
        balloon.addEventListener('touchstart', (e) => {
            e.preventDefault();
            this.popBalloon(balloon);
        });

        document.getElementById('balloons-area').appendChild(balloon);
        this.balloons.push(balloon);

        setTimeout(() => {
            if (balloon.parentNode) {
                balloon.remove();
                this.balloons = this.balloons.filter(b => b !== balloon);
            }
        }, this.balloonLifetime);
    }

    popBalloon(balloon) {
        if (balloon.classList.contains('popped')) return;

        balloon.classList.add('popped');
        this.createParticles(balloon);
        this.playPop();

        this.score++;
        this.updateUI();
        this.checkLevelComplete();

        setTimeout(() => {
            if (balloon.parentNode) {
                balloon.remove();
            }
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

    cleanupBalloons() {
        const now = Date.now();
        this.balloons.forEach(balloon => {
            if (now - balloon.spawnTime > this.balloonLifetime && balloon.parentNode) {
                balloon.remove();
            }
        });
        this.balloons = this.balloons.filter(b => b.parentNode);
    }

    checkLevelComplete() {
        const level = this.levels[this.currentLevel];
        if (this.score >= level.target) {
            this.endLevel(true);
        }
    }

    endLevel(success) {
        clearInterval(this.timerInterval);
        clearInterval(this.spawnInterval);

        this.balloons.forEach(b => b.remove());
        this.balloons = [];

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
        const overlay = document.getElementById('result-overlay');
        const icon = document.getElementById('result-icon');
        const title = document.getElementById('result-title');
        const message = document.getElementById('result-message');
        const buttons = document.querySelector('.result-buttons');

        icon.textContent = '🎉';
        title.textContent = '過關了！';
        message.textContent = '準備進入下一關...';
        buttons.style.display = 'none';

        overlay.classList.remove('hidden');

        setTimeout(() => {
            overlay.classList.add('hidden');
            buttons.style.display = 'flex';
            callback();
        }, 1500);
    }

    showResult(won, reason) {
        const overlay = document.getElementById('result-overlay');
        const icon = document.getElementById('result-icon');
        const title = document.getElementById('result-title');
        const message = document.getElementById('result-message');
        const buttons = document.querySelector('.result-buttons');

        buttons.style.display = 'flex';

        if (won && reason === 'complete') {
            icon.textContent = '🎉';
            title.textContent = '太棒了！全部通關！';
            message.textContent = `你成功完成了所有 ${this.levels.length} 個關卡！`;
            this.playCelebration();
        } else if (!won && reason === 'timeout') {
            icon.textContent = '⏰';
            title.textContent = `第 ${this.currentLevel + 1} 關時間到了！`;
            message.textContent = `還差 ${this.levels[this.currentLevel].target - this.score} 個就過關了！再試一次！`;
        }

        overlay.classList.remove('hidden');
    }

    updateUI() {
        document.getElementById('current-level').textContent = this.currentLevel + 1;
        document.getElementById('current-score').textContent = this.score;
        document.getElementById('target-score').textContent = this.levels[this.currentLevel].target;

        const progress = (this.score / this.levels[this.currentLevel].target) * 100;
        document.querySelector('.progress-fill').style.width = progress + '%';
    }

    // 音效系統
    playPop() {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.frequency.value = 800;
        gain.gain.setValueAtTime(0.3, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.1);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.1);
    }

    playBeep(freq) {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0.2, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.15);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.15);
    }

    playCelebration() {
        const notes = [523, 587, 659, 698, 784];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playBeep(freq), i * 100);
        });
    }

    playLevelComplete() {
        const notes = [440, 523, 659];
        notes.forEach((freq, i) => {
            setTimeout(() => {
                const osc = this.audioCtx.createOscillator();
                const gain = this.audioCtx.createGain();
                osc.connect(gain);
                gain.connect(this.audioCtx.destination);

                osc.frequency.value = freq;
                gain.gain.setValueAtTime(0.25, this.audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.3);

                osc.start();
                osc.stop(this.audioCtx.currentTime + 0.3);
            }, i * 150);
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
                gain.gain.setValueAtTime(0.2, this.audioCtx.currentTime);
                gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.4);

                osc.start();
                osc.stop(this.audioCtx.currentTime + 0.4);
            }, i * 200);
        });
    }
}

// 全域遊戲實例
let game = null;

function startGameWithDifficulty(difficulty) {
    if (!game) {
        game = new BalloonPopGame();
    }
    game.startGameWithDifficulty(difficulty);
}

window.addEventListener('load', () => {
    game = new BalloonPopGame();
});

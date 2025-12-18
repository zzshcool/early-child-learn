/**
 * BaseGame - 全局高品質遊戲基底類別
 * 提供 4-6 歲幼兒遊戲所需的標準生命週期、計時器、音效與 UI 邏輯。
 */
class BaseGame {
    constructor(gameId, levels = []) {
        this.gameId = gameId; // 遊戲唯一標識，用於獎勵系統
        this.levels = levels;
        this.currentLevel = 0;
        this.score = 0;
        this.timeLeft = 0;
        this.gameState = 'IDLE'; // IDLE, TRANSITION, PLAYING, ENDED
        this.timerInterval = null;
        this.idleTimer = null;
        this.idleThreshold = 5000; // 5秒停頓提示
    }

    // ===== 生命週期介面 (需由子類實作) =====

    /** 初始化遊戲元素 */
    onInit() { }
    /** 難度設定時的邏輯 */
    onDifficultySet(difficulty) { }
    /** 關卡開始前的邏輯 */
    onLevelStart() { }
    /** 遊戲主循環或生成邏輯 */
    onUpdate(deltaTime) { }
    /** 處理玩家交互 */
    onHandleInteract(data) { }
    /** 關卡結束邏輯 */
    onLevelEnd(success) { }

    // ===== 核心控制邏輯 =====

    init() {
        console.log(`[BaseGame] Initializing ${this.gameId}...`);
        this.cacheSharedElements();
        this.startBackgroundEffects();
        this.onInit();
        this.setupIdleTracker();
    }

    cacheSharedElements() {
        this.ui = {
            score: document.getElementById('score') || document.getElementById('current-score'),
            time: document.getElementById('time') || document.getElementById('time-left'),
            level: document.getElementById('current-level'),
            progress: document.querySelector('.progress-fill'),
            timerContainer: document.getElementById('timer-display'),
            overlays: {
                difficulty: document.getElementById('difficulty-overlay'),
                transition: document.getElementById('level-transition') || document.getElementById('result-overlay'),
                message: document.getElementById('message-overlay') || document.getElementById('result-overlay')
            }
        };
    }

    /** 啟動難度選擇後進入遊戲 */
    startWithDifficulty(difficulty) {
        this.difficulty = difficulty;
        this.onDifficultySet(difficulty);
        if (this.ui.overlays.difficulty) {
            this.ui.overlays.difficulty.classList.add('hidden');
        }

        // 啟動獎勵系統顯示
        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.createStarDisplay();
        }

        this.startLevel(0);
    }

    startLevel(index) {
        this.currentLevel = index;
        this.score = 0;
        this.gameState = 'TRANSITION';

        const levelData = this.levels[index];
        this.timeLeft = levelData.time;

        this.showLevelTransition(index, () => {
            this.gameState = 'PLAYING';
            this.updateUI();
            this.startTimer();
            this.onLevelStart();
            this.playWelcomeVoice();
        });
    }

    showLevelTransition(index, callback) {
        const overlay = this.ui.overlays.transition;
        if (!overlay) {
            callback();
            return;
        }

        // 更新過場文字 (Thymeleaf/Static 兼容處理)
        const levelNumEl = document.getElementById('transition-level-num');
        const timeEl = document.getElementById('transition-time');
        const targetEl = document.getElementById('transition-target');
        const countdownEl = document.getElementById('countdown');

        if (levelNumEl) levelNumEl.textContent = index + 1;
        if (timeEl) timeEl.textContent = this.levels[index].time;
        if (targetEl) targetEl.textContent = this.levels[index].target;

        overlay.classList.remove('hidden');

        let count = 3;
        if (countdownEl) {
            countdownEl.textContent = count;
            const int = setInterval(() => {
                count--;
                if (count > 0) {
                    countdownEl.textContent = count;
                    GameSound.play('click');
                } else {
                    countdownEl.textContent = 'GO!';
                    GameSound.play('correct');
                    clearInterval(int);
                    setTimeout(() => {
                        overlay.classList.add('hidden');
                        callback();
                    }, 500);
                }
            }, 1000);
        } else {
            callback();
        }
    }

    startTimer() {
        if (this.timerInterval) clearInterval(this.timerInterval);
        this.timerInterval = setInterval(() => {
            if (this.gameState !== 'PLAYING') return;

            this.timeLeft--;
            this.updateTimerUI();

            if (this.timeLeft <= 0) {
                this.endLevel(false);
            }
        }, 1000);
    }

    updateTimerUI() {
        if (this.ui.time) this.ui.time.textContent = this.timeLeft;
        if (this.ui.timerContainer) {
            if (this.timeLeft <= 5) {
                this.ui.timerContainer.classList.add('warning');
            } else {
                this.ui.timerContainer.classList.remove('warning');
            }
        }
    }

    endLevel(success) {
        this.gameState = 'ENDED';
        clearInterval(this.timerInterval);
        this.onLevelEnd(success);

        if (success) {
            const isLast = this.currentLevel === this.levels.length - 1;
            this.showResultMessage(true, isLast);
        } else {
            this.showResultMessage(false);
        }
    }

    showResultMessage(won, isFinal = false) {
        const overlay = this.ui.overlays.message;
        const msgEl = document.getElementById('message-text') || document.getElementById('result-title');

        if (overlay) {
            overlay.classList.remove('hidden');
            // 確保按鈕有 Q 彈類
            const btns = overlay.querySelectorAll('.btn');
            btns.forEach(btn => btn.classList.add('btn-elastic'));
        }

        if (won) {
            GameAudio.correct();
            GameSound.play('win');
            if (isFinal) {
                if (msgEl) msgEl.textContent = '🎉 太棒了！任務圓滿達成！ 🏆';
                if (typeof RewardSystem !== 'undefined') {
                    RewardSystem.recordGameComplete(this.gameId);
                    RewardSystem.addStars(3); // 通關額外獎勵
                }
            } else {
                if (msgEl) msgEl.textContent = `第 ${this.currentLevel + 1} 關成功！`;
                setTimeout(() => {
                    if (overlay) overlay.classList.add('hidden');
                    this.startLevel(this.currentLevel + 1);
                }, 2000);
            }
        } else {
            GameAudio.tryAgain();
            GameSound.play('wrong');
            if (msgEl) msgEl.textContent = `加油！再試一次吧！`;
        }
    }

    // ===== 幼兒專屬引導系統 =====

    setupIdleTracker() {
        const resetIdle = () => {
            clearTimeout(this.idleTimer);
            this.hideHint();
            if (this.gameState === 'PLAYING') {
                this.idleTimer = setTimeout(() => this.showHint(), this.idleThreshold);
            }
        };

        window.addEventListener('mousedown', resetIdle);
        window.addEventListener('touchstart', resetIdle);
        window.addEventListener('keydown', resetIdle);
    }

    showHint() {
        // 預設尋找畫面上第一個遊戲目標並加上光效
        const targets = document.querySelectorAll('.gift, .balloon, .swimming-fish');
        if (targets.length > 0) {
            targets[0].classList.add('hint-glow');
        }
    }

    hideHint() {
        const hinted = document.querySelectorAll('.hint-glow');
        hinted.forEach(el => el.classList.remove('hint-glow'));
    }

    // ===== 通用工具 =====

    updateUI() {
        if (this.ui.score) this.ui.score.textContent = this.score;
        if (this.ui.level) this.ui.level.textContent = this.currentLevel + 1;

        const target = this.levels[this.currentLevel].target;
        if (this.ui.progress) {
            const progress = Math.min(100, (this.score / target) * 100);
            this.ui.progress.style.width = progress + '%';
        }
    }

    startBackgroundEffects() {
        // 通用背景效果 (如雪花或氣泡)
    }

    playWelcomeVoice() {
        // 可由子類客製化語音指令
    }
}

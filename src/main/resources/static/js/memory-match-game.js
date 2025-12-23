/**
 * MemoryMatchGame - 配對遊戲基類
 * 統一管理數字、英文、注音等配對遊戲的共用邏輯
 */
class MemoryMatchGame {
    /**
     * @param {Object} config 遊戲配置
     * @param {string[]} config.items 配對項目（數字/字母/注音）
     * @param {string} config.gameType 遊戲類型（numbers/english/bopomofo）
     * @param {string} config.welcomeMessage 歡迎訊息
     * @param {string} config.memoryMessage 記憶提示訊息
     */
    constructor(config) {
        this.items = config.items || [];
        this.gameType = config.gameType || 'memory';
        this.welcomeMessage = config.welcomeMessage || '歡迎來到配對遊戲！';
        this.memoryMessage = config.memoryMessage || '記住位置！';

        this.currentCards = [];
        this.flippedCards = [];
        this.matchedPairs = 0;
        this.totalPairs = 0;
        this.gameStarted = false;
        this.startTime = null;
        this.timerInterval = null;
        this.rows = 2;
        this.cols = 2;

        this.elements = {
            sizeSelection: null,
            gameArea: null,
            cardsGrid: null,
            timer: null,
            message: null,
            restartBtn: null,
            backBtn: null
        };
    }

    /**
     * 初始化 DOM 元素引用
     */
    initElements() {
        this.elements = {
            sizeSelection: document.getElementById('size-selection'),
            gameArea: document.getElementById('game-area'),
            cardsGrid: document.getElementById('cards-grid'),
            timer: document.getElementById('timer'),
            message: document.getElementById('message'),
            restartBtn: document.getElementById('restart-btn'),
            backBtn: document.getElementById('back-btn')
        };
    }

    /**
     * 初始化遊戲
     */
    init() {
        this.initElements();

        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.createStarDisplay();
        }

        if (typeof GameAudio !== 'undefined') {
            GameAudio.speak(this.welcomeMessage);
        }
    }

    /**
     * 洗牌演算法
     */
    shuffle(array) {
        const arr = [...array];
        for (let i = arr.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [arr[i], arr[j]] = [arr[j], arr[i]];
        }
        return arr;
    }

    /**
     * 開始遊戲
     */
    startGame(rows, cols) {
        this.rows = rows;
        this.cols = cols;
        this.totalPairs = (rows * cols) / 2;

        // 隱藏選擇界面，顯示遊戲區域
        this.elements.sizeSelection.classList.add('hidden');
        this.elements.gameArea.classList.remove('hidden');

        // 選擇項目並配對
        const selectedItems = this.shuffle(this.items).slice(0, this.totalPairs);
        const cardValues = this.shuffle([...selectedItems, ...selectedItems]);

        this.currentCards = cardValues.map((value, index) => ({
            id: index,
            value: value,
            matched: false
        }));

        // 設置網格
        this.elements.cardsGrid.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
        this.elements.cardsGrid.innerHTML = '';

        // 渲染卡片（正面朝上）
        this.currentCards.forEach(card => {
            const cardEl = document.createElement('div');
            cardEl.className = 'memory-card';
            cardEl.textContent = card.value;
            cardEl.dataset.id = card.id;
            cardEl.onclick = () => this.handleCardClick(cardEl, card);
            this.elements.cardsGrid.appendChild(cardEl);
        });

        // 重置遊戲狀態
        this.flippedCards = [];
        this.matchedPairs = 0;
        this.gameStarted = false;
        this.startTime = null;

        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }

        this.elements.timer.textContent = '00:00';
        this.elements.message.textContent = this.memoryMessage;
        this.elements.restartBtn.classList.add('hidden');
        this.elements.backBtn.classList.add('hidden');

        // 倒數計時
        this.startCountdown();
    }

    /**
     * 開始倒數
     */
    startCountdown() {
        let countdown = 3;
        this.elements.message.innerHTML = `<span class="countdown-big">${countdown}</span>`;

        if (typeof GameAudio !== 'undefined') {
            GameAudio.speak(this.memoryMessage);
        }

        const countdownTimer = setInterval(() => {
            countdown--;
            if (countdown > 0) {
                this.elements.message.innerHTML = `<span class="countdown-big">${countdown}</span>`;
            } else {
                clearInterval(countdownTimer);
                this.flipAllCards();
                this.elements.message.textContent = '開始配對！';
                this.gameStarted = true;
                this.startTimer();

                if (typeof GameAudio !== 'undefined') {
                    GameAudio.speak('開始！');
                }
            }
        }, 1000);
    }

    /**
     * 翻轉所有卡片（蓋起來）
     */
    flipAllCards() {
        document.querySelectorAll('.memory-card').forEach(card => {
            card.classList.add('flipped');
        });
    }

    /**
     * 開始計時
     */
    startTimer() {
        this.startTime = Date.now();
        this.timerInterval = setInterval(() => this.updateTimer(), 100);
    }

    /**
     * 更新計時器
     */
    updateTimer() {
        if (!this.startTime) return;

        const elapsed = Date.now() - this.startTime;
        const seconds = Math.floor(elapsed / 1000);
        const milliseconds = Math.floor((elapsed % 1000) / 100);
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;

        this.elements.timer.textContent =
            `${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}.${milliseconds}`;
    }

    /**
     * 處理卡片點擊
     */
    handleCardClick(cardEl, card) {
        if (!this.gameStarted) return;
        if (card.matched) return;
        if (this.flippedCards.length >= 2) return;
        if (this.flippedCards.some(c => c.id === card.id)) return;

        // 翻開卡片
        cardEl.classList.remove('flipped');
        this.flippedCards.push({ el: cardEl, card: card });

        if (this.flippedCards.length === 2) {
            this.checkMatch();
        }
    }

    /**
     * 檢查是否配對
     */
    checkMatch() {
        const [first, second] = this.flippedCards;

        if (first.card.value === second.card.value) {
            setTimeout(() => {
                first.el.classList.add('matched');
                second.el.classList.add('matched');
                first.card.matched = true;
                second.card.matched = true;
                this.matchedPairs++;

                this.flippedCards = [];

                if (typeof GameSound !== 'undefined') GameSound.play('correct');
                if (typeof GameAudio !== 'undefined') GameAudio.speak('答對了！');
                if (typeof RewardSystem !== 'undefined') RewardSystem.addStars(1);

                if (this.matchedPairs === this.totalPairs) {
                    this.gameWon();
                }
            }, 300);
        } else {
            this.gameStarted = false;
            first.el.classList.add('shake');
            second.el.classList.add('shake');

            if (typeof GameSound !== 'undefined') GameSound.play('wrong');

            setTimeout(() => {
                first.el.classList.remove('shake');
                second.el.classList.remove('shake');
                first.el.classList.add('flipped');
                second.el.classList.add('flipped');
                this.flippedCards = [];
                this.gameStarted = true;
            }, 800);
        }
    }

    /**
     * 遊戲勝利
     */
    gameWon() {
        clearInterval(this.timerInterval);
        this.gameStarted = false;

        const elapsed = Date.now() - this.startTime;
        const seconds = (elapsed / 1000).toFixed(1);

        this.elements.message.innerHTML = `🎉 恭喜完成！<br>用時：${seconds} 秒`;
        this.elements.restartBtn.classList.remove('hidden');
        this.elements.backBtn.classList.remove('hidden');

        if (typeof GameSound !== 'undefined') GameSound.play('win');
        if (typeof GameAudio !== 'undefined') {
            GameAudio.speak(`太棒了！你用了 ${Math.floor(seconds)} 秒完成遊戲！`);
        }
        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.addStars(3);
            RewardSystem.recordGameComplete(this.gameType);
        }
    }

    /**
     * 重新開始
     */
    resetGame() {
        this.startGame(this.rows, this.cols);
    }

    /**
     * 返回選擇
     */
    backToSelection() {
        this.elements.sizeSelection.classList.remove('hidden');
        this.elements.gameArea.classList.add('hidden');

        if (this.timerInterval) {
            clearInterval(this.timerInterval);
        }
    }
}

// 全域匯出
window.MemoryMatchGame = MemoryMatchGame;

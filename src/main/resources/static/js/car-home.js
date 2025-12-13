/**
 * 小車回家 - 遊戲引擎
 * 玩法：點擊相鄰格子讓小車移動到房子
 * 專為 3-6 歲幼兒設計
 */
class CarHomeGame {
    constructor() {
        // 五個關卡設定
        this.levels = [
            {
                name: '第一關',
                gridSize: 3,
                carStart: { row: 0, col: 0 },
                homePos: { row: 2, col: 2 },
                obstacles: []
            },
            {
                name: '第二關',
                gridSize: 3,
                carStart: { row: 0, col: 0 },
                homePos: { row: 2, col: 2 },
                obstacles: [{ row: 1, col: 1, type: 'rock' }]
            },
            {
                name: '第三關',
                gridSize: 4,
                carStart: { row: 0, col: 0 },
                homePos: { row: 3, col: 3 },
                obstacles: [
                    { row: 1, col: 1, type: 'water' },
                    { row: 2, col: 0, type: 'rock' }
                ]
            },
            {
                name: '第四關',
                gridSize: 4,
                carStart: { row: 0, col: 0 },
                homePos: { row: 3, col: 3 },
                obstacles: [
                    { row: 0, col: 2, type: 'rock' },
                    { row: 1, col: 1, type: 'water' },
                    { row: 2, col: 2, type: 'rock' }
                ]
            },
            {
                name: '第五關',
                gridSize: 5,
                carStart: { row: 0, col: 0 },
                homePos: { row: 4, col: 4 },
                obstacles: [
                    { row: 0, col: 2, type: 'rock' },
                    { row: 1, col: 1, type: 'water' },
                    { row: 2, col: 3, type: 'rock' },
                    { row: 3, col: 0, type: 'water' },
                    { row: 3, col: 2, type: 'rock' }
                ]
            }
        ];

        this.currentLevel = 0;
        this.carPosition = { row: 0, col: 0 };
        this.moveCount = 0;
        this.gameActive = false;
        this.grid = [];

        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }

    init() {
        this.gameBoard = document.getElementById('game-board');
        this.levelIndicator = document.getElementById('current-level');
        this.moveCounter = document.getElementById('move-count');
    }

    startGame() {
        document.getElementById('tutorial-overlay').classList.add('hidden');
        document.getElementById('game-ui').classList.remove('hidden');

        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }

        // 語音提示
        if (window.SpeechHelper) {
            SpeechHelper.speak('點擊亮亮的格子，幫小車回家！');
        }

        this.startLevel(0);
    }

    startLevel(levelIndex) {
        this.currentLevel = levelIndex;
        this.moveCount = 0;
        this.gameActive = false;

        const level = this.levels[levelIndex];
        this.carPosition = { ...level.carStart };

        this.showLevelTransition(levelIndex, () => {
            this.gameActive = true;
            this.buildBoard();
            this.updateUI();
        });
    }

    showLevelTransition(levelIndex, callback) {
        const overlay = document.getElementById('level-transition');
        const level = this.levels[levelIndex];

        document.getElementById('transition-level-num').textContent = levelIndex + 1;
        document.getElementById('transition-grid-size').textContent = `${level.gridSize}x${level.gridSize}`;
        document.getElementById('transition-obstacles').textContent = level.obstacles.length;

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
                countdownEl.textContent = '出發！';
                this.playBeep(800);
                clearInterval(countInterval);

                // 語音提示
                if (window.SpeechHelper) {
                    SpeechHelper.speak('出發！');
                }

                setTimeout(() => {
                    overlay.classList.add('hidden');
                    callback();
                }, 500);
            }
        }, 1000);
    }

    buildBoard() {
        const level = this.levels[this.currentLevel];
        const gridSize = level.gridSize;

        this.gameBoard.innerHTML = '';
        this.gameBoard.style.gridTemplateColumns = `repeat(${gridSize}, 1fr)`;
        this.grid = [];

        for (let row = 0; row < gridSize; row++) {
            this.grid[row] = [];
            for (let col = 0; col < gridSize; col++) {
                const cell = document.createElement('div');
                cell.className = 'grid-cell';
                cell.dataset.row = row;
                cell.dataset.col = col;

                // 檢查是否為障礙物
                const obstacle = level.obstacles.find(o => o.row === row && o.col === col);

                let cellType = 'empty';

                if (row === this.carPosition.row && col === this.carPosition.col) {
                    cellType = 'car';
                    cell.innerHTML = '<span class="car-emoji">🚗</span>';
                } else if (row === level.homePos.row && col === level.homePos.col) {
                    cellType = 'home';
                    cell.innerHTML = '<span class="home-emoji">🏠</span>';
                } else if (obstacle) {
                    cellType = obstacle.type;
                    if (obstacle.type === 'rock') {
                        cell.innerHTML = '<span class="obstacle-emoji">🪨</span>';
                    } else if (obstacle.type === 'water') {
                        cell.innerHTML = '<span class="obstacle-emoji">🌊</span>';
                    }
                    cell.classList.add('obstacle');
                }

                cell.dataset.type = cellType;

                // 點擊事件
                cell.addEventListener('click', () => this.handleCellClick(row, col));
                cell.addEventListener('touchstart', (e) => {
                    e.preventDefault();
                    this.handleCellClick(row, col);
                }, { passive: false });

                this.gameBoard.appendChild(cell);
                this.grid[row][col] = { element: cell, type: cellType };
            }
        }

        this.highlightMoveOptions();
    }

    highlightMoveOptions() {
        // 清除所有高亮
        document.querySelectorAll('.grid-cell').forEach(cell => {
            cell.classList.remove('can-move', 'pulse');
        });

        if (!this.gameActive) return;

        const level = this.levels[this.currentLevel];
        const { row, col } = this.carPosition;

        // 四個方向
        const directions = [
            { dr: -1, dc: 0, name: '上' },
            { dr: 1, dc: 0, name: '下' },
            { dr: 0, dc: -1, name: '左' },
            { dr: 0, dc: 1, name: '右' }
        ];

        for (const { dr, dc } of directions) {
            const newRow = row + dr;
            const newCol = col + dc;

            if (this.canMoveTo(newRow, newCol)) {
                const cell = this.grid[newRow][newCol].element;
                cell.classList.add('can-move', 'pulse');
            }
        }
    }

    canMoveTo(row, col) {
        const level = this.levels[this.currentLevel];

        // 邊界檢查
        if (row < 0 || row >= level.gridSize || col < 0 || col >= level.gridSize) {
            return false;
        }

        // 障礙物檢查
        const cellType = this.grid[row][col].type;
        if (cellType === 'rock' || cellType === 'water') {
            return false;
        }

        return true;
    }

    handleCellClick(row, col) {
        if (!this.gameActive) return;

        const { row: carRow, col: carCol } = this.carPosition;

        // 檢查是否為相鄰格子
        const isAdjacent = (Math.abs(row - carRow) + Math.abs(col - carCol)) === 1;

        if (!isAdjacent) {
            // 語音提示
            if (window.SpeechHelper) {
                SpeechHelper.speak('點擊旁邊亮亮的格子喔！');
            }
            return;
        }

        if (!this.canMoveTo(row, col)) {
            this.playError();

            const cellType = this.grid[row][col].type;
            if (window.SpeechHelper) {
                if (cellType === 'rock') {
                    SpeechHelper.speak('石頭擋住了！換一條路吧！');
                } else if (cellType === 'water') {
                    SpeechHelper.speak('有水坑！小車不能過去！');
                }
            }

            // 震動效果
            this.grid[row][col].element.classList.add('shake');
            setTimeout(() => {
                this.grid[row][col].element.classList.remove('shake');
            }, 500);
            return;
        }

        // 移動小車
        this.moveCar(row, col);
    }

    moveCar(newRow, newCol) {
        const level = this.levels[this.currentLevel];
        const { row: oldRow, col: oldCol } = this.carPosition;

        // 更新舊格子
        const oldCell = this.grid[oldRow][oldCol].element;
        oldCell.innerHTML = '';
        oldCell.dataset.type = 'empty';
        this.grid[oldRow][oldCol].type = 'empty';

        // 更新新格子
        const newCell = this.grid[newRow][newCol].element;

        // 檢查是否到達房子
        const isHome = newRow === level.homePos.row && newCol === level.homePos.col;

        if (!isHome) {
            newCell.innerHTML = '<span class="car-emoji">🚗</span>';
            newCell.dataset.type = 'car';
            this.grid[newRow][newCol].type = 'car';
        } else {
            // 小車和房子一起顯示
            newCell.innerHTML = '<span class="car-emoji arriving">🚗</span><span class="home-emoji">🏠</span>';
        }

        // 更新位置
        this.carPosition = { row: newRow, col: newCol };
        this.moveCount++;
        this.updateUI();

        // 音效
        this.playMove();

        // 語音方向提示
        if (window.SpeechHelper) {
            const dir = this.getDirection(oldRow, oldCol, newRow, newCol);
            if (Math.random() < 0.3) { // 30% 機率語音
                SpeechHelper.speak(`往${dir}走！`);
            }
        }

        if (isHome) {
            this.levelComplete();
        } else {
            this.highlightMoveOptions();
        }
    }

    getDirection(fromRow, fromCol, toRow, toCol) {
        if (toRow < fromRow) return '上';
        if (toRow > fromRow) return '下';
        if (toCol < fromCol) return '左';
        return '右';
    }

    levelComplete() {
        this.gameActive = false;
        this.playWin();

        if (this.currentLevel < this.levels.length - 1) {
            // 還有下一關
            if (window.SpeechHelper) {
                SpeechHelper.speak('太棒了！小車到家了！', () => {
                    setTimeout(() => this.startLevel(this.currentLevel + 1), 1000);
                });
            } else {
                setTimeout(() => this.startLevel(this.currentLevel + 1), 1500);
            }
        } else {
            // 全部通關
            if (window.SpeechHelper) {
                SpeechHelper.speak('恭喜你！全部通關了！你好棒棒！');
            }
            setTimeout(() => {
                document.getElementById('result-overlay').classList.remove('hidden');
            }, 1000);
        }
    }

    updateUI() {
        if (this.levelIndicator) {
            this.levelIndicator.textContent = this.currentLevel + 1;
        }
        if (this.moveCounter) {
            this.moveCounter.textContent = this.moveCount;
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

    playMove() {
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.frequency.setValueAtTime(400, this.audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(500, this.audioCtx.currentTime + 0.1);
        gain.gain.setValueAtTime(0.12, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.15);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.15);
    }

    playError() {
        if (this.audioCtx.state === 'suspended') this.audioCtx.resume();
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.type = 'sawtooth';
        osc.frequency.value = 150;
        gain.gain.setValueAtTime(0.1, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + 0.2);

        osc.start();
        osc.stop(this.audioCtx.currentTime + 0.2);
    }

    playWin() {
        const notes = [523, 587, 659, 784, 880];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playBeep(freq), i * 120);
        });
    }
}

// 全域遊戲實例
let game = null;

function startGame() {
    if (!game) {
        game = new CarHomeGame();
        game.init();
    }
    game.startGame();
}

window.addEventListener('load', () => {
    game = new CarHomeGame();
    game.init();
});

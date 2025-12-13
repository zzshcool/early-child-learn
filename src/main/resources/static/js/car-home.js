const gridSize = 5;
const gameBoard = document.getElementById('game-board');
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

let grid = []; // 儲存每個格子的資訊
let carPosition = { row: 0, col: 0 };
let homePosition = { row: 4, col: 4 };
let gameActive = false;

// 道路連接方向定義
const tileConnections = {
    'straight-h': { left: true, right: true, top: false, bottom: false },
    'straight-v': { left: false, right: false, top: true, bottom: true },
    'corner-tl': { left: true, top: true, right: false, bottom: false },
    'corner-tr': { right: true, top: true, left: false, bottom: false },
    'corner-bl': { left: true, bottom: true, right: false, top: false },
    'corner-br': { right: true, bottom: true, left: false, top: false }
};

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    gameActive = true;
    if (audioCtx.state === 'suspended') audioCtx.resume();
    initBoard();
}

function initBoard() {
    gameBoard.innerHTML = '';
    grid = [];

    // 建立 5x5 網格
    for (let row = 0; row < gridSize; row++) {
        grid[row] = [];
        for (let col = 0; col < gridSize; col++) {
            const cell = document.createElement('div');
            cell.className = 'grid-cell';
            cell.dataset.row = row;
            cell.dataset.col = col;

            // 小車位置（左上角）
            if (row === 0 && col === 0) {
                const car = document.createElement('div');
                car.className = 'car';
                car.textContent = '🚗';
                cell.appendChild(car);
            }

            // 房子位置（右下角）
            if (row === 4 && col === 4) {
                cell.textContent = '🏠';
                cell.style.fontSize = '3rem';
            }

            // 拖放事件
            cell.addEventListener('dragover', handleDragOver);
            cell.addEventListener('drop', handleDrop);
            cell.addEventListener('dragleave', handleDragLeave);

            gameBoard.appendChild(cell);
            grid[row][col] = { element: cell, tile: null };
        }
    }

    // 設定道路磚塊的拖動事件
    setupTileDragging();
}

function setupTileDragging() {
    const tileOptions = document.querySelectorAll('.tile-option');
    tileOptions.forEach(option => {
        option.addEventListener('dragstart', handleDragStart);

        // 觸控支援
        option.addEventListener('touchstart', handleTouchStart, { passive: false });
    });
}

let draggedTileType = null;
let touchClone = null;

function handleDragStart(e) {
    draggedTileType = e.target.dataset.type;
    e.dataTransfer.effectAllowed = 'copy';
}

function handleTouchStart(e) {
    e.preventDefault();
    const option = e.target.closest('.tile-option');
    if (!option) return;

    draggedTileType = option.dataset.type;

    // 建立視覺化的拖動元素
    touchClone = option.cloneNode(true);
    touchClone.style.position = 'fixed';
    touchClone.style.pointerEvents = 'none';
    touchClone.style.opacity = '0.7';
    touchClone.style.zIndex = '1000';
    document.body.appendChild(touchClone);

    document.addEventListener('touchmove', handleTouchMove);
    document.addEventListener('touchend', handleTouchEnd);

    updateTouchClonePosition(e.touches[0]);
}

function handleTouchMove(e) {
    if (touchClone) {
        updateTouchClonePosition(e.touches[0]);
    }
}

function updateTouchClonePosition(touch) {
    touchClone.style.left = (touch.clientX - 30) + 'px';
    touchClone.style.top = (touch.clientY - 30) + 'px';
}

function handleTouchEnd(e) {
    document.removeEventListener('touchmove', handleTouchMove);
    document.removeEventListener('touchend', handleTouchEnd);

    if (touchClone) {
        const touch = e.changedTouches[0];
        const target = document.elementFromPoint(touch.clientX, touch.clientY);
        const cell = target?.closest('.grid-cell');

        if (cell) {
            placeTile(cell, draggedTileType);
        }

        touchClone.remove();
        touchClone = null;
    }

    draggedTileType = null;
}

function handleDragOver(e) {
    e.preventDefault();
    e.currentTarget.classList.add('drag-over');
}

function handleDragLeave(e) {
    e.currentTarget.classList.remove('drag-over');
}

function handleDrop(e) {
    e.preventDefault();
    const cell = e.currentTarget;
    cell.classList.remove('drag-over');

    placeTile(cell, draggedTileType);
    draggedTileType = null;
}

function placeTile(cell, tileType) {
    const row = parseInt(cell.dataset.row);
    const col = parseInt(cell.dataset.col);

    // 不能在小車或房子位置放置
    if ((row === 0 && col === 0) || (row === 4 && col === 4)) {
        playSound('error');
        return;
    }

    // 移除舊的道路磚塊
    const existingTile = cell.querySelector('.road-tile');
    if (existingTile) {
        existingTile.remove();
    }

    // 放置新的道路磚塊
    const tile = document.createElement('div');
    tile.className = `road-tile ${tileType}`;

    // 保留房子 emoji
    if (cell.textContent !== '🏠') {
        cell.textContent = '';
    }

    cell.appendChild(tile);
    grid[row][col].tile = tileType;

    playSound('place');

    // 檢查路徑是否完成
    setTimeout(() => checkPath(), 100);
}

function checkPath() {
    // 使用 BFS 檢查從小車到房子是否有路徑
    const visited = Array(gridSize).fill(null).map(() => Array(gridSize).fill(false));
    const queue = [{ row: 0, col: 0 }];
    visited[0][0] = true;

    while (queue.length > 0) {
        const { row, col } = queue.shift();

        // 到達房子
        if (row === homePosition.row && col === homePosition.col) {
            // 路徑完成！
            setTimeout(() => moveCar(), 500);
            return;
        }

        const currentTile = grid[row][col].tile;
        if (!currentTile && !(row === 0 && col === 0)) continue;

        const connections = currentTile ? tileConnections[currentTile] :
            { left: true, right: true, top: true, bottom: true };

        // 檢查四個方向
        const directions = [
            { dr: -1, dc: 0, current: 'top', next: 'bottom' }, // 上
            { dr: 1, dc: 0, current: 'bottom', next: 'top' }, // 下
            { dr: 0, dc: -1, current: 'left', next: 'right' }, // 左
            { dr: 0, dc: 1, current: 'right', next: 'left' } // 右
        ];

        for (const { dr, dc, current, next } of directions) {
            const newRow = row + dr;
            const newCol = col + dc;

            if (newRow < 0 || newRow >= gridSize || newCol < 0 || newCol >= gridSize) continue;
            if (visited[newRow][newCol]) continue;
            if (!connections[current]) continue;

            const nextTile = grid[newRow][newCol].tile;
            if (!nextTile && !(newRow === homePosition.row && newCol === homePosition.col)) continue;

            const nextConnections = nextTile ? tileConnections[nextTile] :
                { left: true, right: true, top: true, bottom: true };

            if (nextConnections[next]) {
                visited[newRow][newCol] = true;
                queue.push({ row: newRow, col: newCol });
            }
        }
    }
}

function moveCar() {
    // 找出路徑並移動小車
    const path = findPath();
    if (!path || path.length === 0) return;

    const car = document.querySelector('.car');
    let step = 0;

    function moveStep() {
        if (step >= path.length) {
            // 到達終點
            endGame();
            return;
        }

        const { row, col } = path[step];
        const cell = grid[row][col].element;
        const rect = cell.getBoundingClientRect();
        const boardRect = gameBoard.getBoundingClientRect();

        car.classList.add('moving');
        car.style.left = (rect.left - boardRect.left + rect.width / 2 - 25) + 'px';
        car.style.top = (rect.top - boardRect.top + rect.height / 2 - 25) + 'px';

        playSound('move');

        step++;
        setTimeout(() => {
            car.classList.remove('moving');
            moveStep();
        }, 600);
    }

    moveStep();
}

function findPath() {
    // BFS 找路徑
    const visited = Array(gridSize).fill(null).map(() => Array(gridSize).fill(false));
    const parent = Array(gridSize).fill(null).map(() => Array(gridSize).fill(null));
    const queue = [{ row: 0, col: 0 }];
    visited[0][0] = true;

    while (queue.length > 0) {
        const { row, col } = queue.shift();

        if (row === homePosition.row && col === homePosition.col) {
            // 重建路徑
            const path = [];
            let current = { row, col };
            while (current) {
                path.unshift(current);
                current = parent[current.row][current.col];
            }
            return path;
        }

        const currentTile = grid[row][col].tile;
        const connections = currentTile ? tileConnections[currentTile] :
            { left: true, right: true, top: true, bottom: true };

        const directions = [
            { dr: -1, dc: 0, current: 'top', next: 'bottom' },
            { dr: 1, dc: 0, current: 'bottom', next: 'top' },
            { dr: 0, dc: -1, current: 'left', next: 'right' },
            { dr: 0, dc: 1, current: 'right', next: 'left' }
        ];

        for (const { dr, dc, current, next } of directions) {
            const newRow = row + dr;
            const newCol = col + dc;

            if (newRow < 0 || newRow >= gridSize || newCol < 0 || newCol >= gridSize) continue;
            if (visited[newRow][newCol]) continue;
            if (!connections[current]) continue;

            const nextTile = grid[newRow][newCol].tile;
            if (!nextTile && !(newRow === homePosition.row && newCol === homePosition.col)) continue;

            const nextConnections = nextTile ? tileConnections[nextTile] :
                { left: true, right: true, top: true, bottom: true };

            if (nextConnections[next]) {
                visited[newRow][newCol] = true;
                parent[newRow][newCol] = { row, col };
                queue.push({ row: newRow, col: newCol });
            }
        }
    }

    return null;
}

function playSound(type) {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (type === 'place') {
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.1);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.1);
    } else if (type === 'move') {
        osc.frequency.setValueAtTime(300, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(350, audioCtx.currentTime + 0.2);
        gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.2);
        osc.type = 'sine';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.2);
    } else if (type === 'error') {
        osc.frequency.setValueAtTime(200, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        osc.type = 'sawtooth';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.2);
    }
}

function playWinSound() {
    const notes = [523.25, 659.25, 783.99, 1046.50];
    notes.forEach((freq, i) => {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.connect(gain);
        gain.connect(audioCtx.destination);

        osc.frequency.value = freq;
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime + i * 0.15);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + i * 0.15 + 0.3);

        osc.start(audioCtx.currentTime + i * 0.15);
        osc.stop(audioCtx.currentTime + i * 0.15 + 0.3);
    });
}

function endGame() {
    playWinSound();
    setTimeout(() => {
        document.getElementById('message-overlay').classList.remove('hidden');
    }, 1000);
}

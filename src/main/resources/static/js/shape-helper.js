/**
 * 形狀小幫手 - 遊戲引擎
 * 專為 3-6 歲幼兒設計，包含語音朗讀
 */
const shapes = [
    { type: 'circle', img: '/images/games/shape-helper/circle.png', name: '圓形' },
    { type: 'triangle', img: '/images/games/shape-helper/triangle.png', name: '三角形' },
    { type: 'square', img: '/images/games/shape-helper/square.png', name: '正方形' }
];

let itemsProcessed = 0;
const totalItems = shapes.length;
let currentDragging = null;

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');

    // 語音說明
    if (typeof GameAudio !== 'undefined') {
        GameAudio.speak('把形狀拖到正確的洞裡！', init);
    } else {
        init();
    }
}

function init() {
    const pool = document.getElementById('shapes-pool');

    // 隨機排列形狀
    const shuffled = [...shapes].sort(() => Math.random() - 0.5);

    shuffled.forEach(shape => {
        const el = document.createElement('div');
        el.className = 'draggable-shape';
        el.dataset.shape = shape.type;
        el.dataset.name = shape.name;

        const img = document.createElement('img');
        img.src = shape.img;
        img.alt = shape.name;
        el.appendChild(img);

        // 名稱標籤
        const label = document.createElement('div');
        label.className = 'shape-label';
        label.textContent = shape.name;
        el.appendChild(label);

        makeDraggable(el);
        pool.appendChild(el);
    });

    updateProgress();
}

// 統一音效系統
function playSound(success) {
    if (typeof GameSound !== 'undefined') {
        GameSound.play(success ? 'correct' : 'wrong');
    }
}

function makeDraggable(el) {
    let startX, startY, initialLeft, initialTop;

    const onStart = (e) => {
        e.preventDefault();
        const touch = e.type === 'touchstart' ? e.touches[0] : e;
        startX = touch.clientX;
        startY = touch.clientY;

        currentDragging = el;

        const rect = el.getBoundingClientRect();
        el.style.position = 'fixed';
        el.style.left = rect.left + 'px';
        el.style.top = rect.top + 'px';
        el.style.width = rect.width + 'px';
        el.style.zIndex = '1000';
        el.classList.add('dragging');

        initialLeft = rect.left;
        initialTop = rect.top;

        // 拿起時播放音效和語音
        if (window.SoundHelper) SoundHelper.play('pop');
        if (window.SpeechHelper) {
            SpeechHelper.speak(el.dataset.name);
        }

        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onEnd);
        document.addEventListener('touchmove', onMove, { passive: false });
        document.addEventListener('touchend', onEnd);
    };

    const onMove = (e) => {
        e.preventDefault();
        const touch = e.type === 'touchmove' ? e.touches[0] : e;
        const dx = touch.clientX - startX;
        const dy = touch.clientY - startY;

        el.style.left = (initialLeft + dx) + 'px';
        el.style.top = (initialTop + dy) + 'px';

        // 高亮目標區
        document.querySelectorAll('.target-slot').forEach(slot => {
            slot.classList.remove('highlight');
        });

        const centerX = initialLeft + dx + el.offsetWidth / 2;
        const centerY = initialTop + dy + el.offsetHeight / 2;
        el.style.visibility = 'hidden';
        const below = document.elementFromPoint(centerX, centerY);
        el.style.visibility = 'visible';

        if (below) {
            const slot = below.closest('.target-slot');
            if (slot && !slot.classList.contains('filled')) {
                slot.classList.add('highlight');
            }
        }
    };

    const onEnd = (e) => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onEnd);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', onEnd);

        el.classList.remove('dragging');

        document.querySelectorAll('.target-slot').forEach(slot => {
            slot.classList.remove('highlight');
        });

        checkDrop(el);
        currentDragging = null;
    };

    el.addEventListener('mousedown', onStart);
    el.addEventListener('touchstart', onStart, { passive: false });
}

function checkDrop(el) {
    el.style.visibility = 'hidden';
    const rect = el.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;

    const belowElement = document.elementFromPoint(centerX, centerY);
    el.style.visibility = 'visible';

    const slot = belowElement ? belowElement.closest('.target-slot') : null;

    if (slot && slot.dataset.shape === el.dataset.shape && !slot.classList.contains('filled')) {
        // 配對成功！
        playSound(true);

        // 語音回饋
        if (window.SpeechHelper) {
            SpeechHelper.correct();
        }

        // 將形狀放入插槽
        const clone = el.querySelector('img').cloneNode(true);
        clone.style.width = '80px';
        clone.style.height = '80px';
        slot.innerHTML = '';
        slot.appendChild(clone);
        slot.classList.add('filled');

        // 成功動畫
        slot.classList.add('success-animation');

        el.remove();
        itemsProcessed++;
        updateProgress();

        if (itemsProcessed >= totalItems) {
            setTimeout(() => {
                if (window.SpeechHelper) {
                    SpeechHelper.win(() => {
                        document.getElementById('message-overlay').classList.remove('hidden');
                    });
                } else {
                    document.getElementById('message-overlay').classList.remove('hidden');
                }
                if (window.SoundHelper) SoundHelper.play('win');
            }, 500);
        }
    } else {
        // 錯誤或未放對
        playSound(false);

        // 語音鼓勵
        if (window.SpeechHelper) {
            SpeechHelper.tryAgain();
        }

        // 回到原位
        el.style.position = '';
        el.style.left = '';
        el.style.top = '';
        el.style.width = '';
        el.style.zIndex = '';
    }
}

function updateProgress() {
    const progressEl = document.getElementById('progress');
    if (progressEl) {
        progressEl.textContent = `${itemsProcessed} / ${totalItems}`;
    }

    const progressFill = document.querySelector('.progress-fill');
    if (progressFill) {
        progressFill.style.width = (itemsProcessed / totalItems * 100) + '%';
    }
}

/**
 * 農場收成日 - 遊戲引擎
 * 專為 3-6 歲幼兒設計，包含語音朗讀
 */
const items = [
    { type: 'veggie', img: '/images/games/farm-harvest/carrot.png', name: '胡蘿蔔' },
    { type: 'veggie', img: '/images/games/farm-harvest/carrot.png', name: '胡蘿蔔' },
    { type: 'fruit', img: '/images/games/farm-harvest/apple.png', name: '蘋果' },
    { type: 'fruit', img: '/images/games/farm-harvest/apple.png', name: '蘋果' },
    { type: 'fruit', img: '/images/games/farm-harvest/banana.png', name: '香蕉' }
];

const typeNames = {
    'veggie': '蔬菜',
    'fruit': '水果'
};

let collected = 0;
const total = items.length;
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    if (audioCtx.state === 'suspended') audioCtx.resume();

    // 語音說明
    if (window.SpeechHelper) {
        SpeechHelper.speak('把蔬菜放到籃子，水果放到箱子！', init);
    } else {
        init();
    }
}

function init() {
    const area = document.getElementById('items-area');
    const shuffled = [...items].sort(() => Math.random() - 0.5);

    shuffled.forEach((item, index) => {
        const el = document.createElement('div');
        el.className = 'harvest-item';
        el.dataset.type = item.type;
        el.dataset.name = item.name;
        el.dataset.index = index;

        const img = document.createElement('img');
        img.src = item.img;
        img.alt = item.name;
        el.appendChild(img);

        // 名稱標籤
        const label = document.createElement('div');
        label.className = 'item-label';
        label.textContent = item.name;
        el.appendChild(label);

        makeDraggable(el);
        area.appendChild(el);
    });

    updateProgress();
}

function playSound(success) {
    if (window.SoundHelper) {
        SoundHelper.play(success ? 'correct' : 'wrong');
        return;
    }

    if (audioCtx.state === 'suspended') audioCtx.resume();
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (success) {
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(600, audioCtx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.2);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.2);
    } else {
        osc.frequency.setValueAtTime(150, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        osc.type = 'sawtooth';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.2);
    }
}

function makeDraggable(el) {
    let startX, startY, initialLeft, initialTop;

    const onStart = (e) => {
        e.preventDefault();
        const touch = e.type === 'touchstart' ? e.touches[0] : e;
        startX = touch.clientX;
        startY = touch.clientY;

        const rect = el.getBoundingClientRect();
        el.style.position = 'fixed';
        el.style.left = rect.left + 'px';
        el.style.top = rect.top + 'px';
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
        document.querySelectorAll('.drop-zone').forEach(zone => {
            zone.classList.remove('highlight');
        });

        const centerX = initialLeft + dx + 40;
        const centerY = initialTop + dy + 40;
        el.style.visibility = 'hidden';
        const below = document.elementFromPoint(centerX, centerY);
        el.style.visibility = 'visible';

        if (below && below.closest('.drop-zone')) {
            below.closest('.drop-zone').classList.add('highlight');
        }
    };

    const onEnd = (e) => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onEnd);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', onEnd);

        el.classList.remove('dragging');

        document.querySelectorAll('.drop-zone').forEach(zone => {
            zone.classList.remove('highlight');
        });

        checkDrop(el);
    };

    el.addEventListener('mousedown', onStart);
    el.addEventListener('touchstart', onStart, { passive: false });
}

function checkDrop(el) {
    const rect = el.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;

    el.style.visibility = 'hidden';
    const below = document.elementFromPoint(centerX, centerY);
    el.style.visibility = 'visible';

    const zone = below ? below.closest('.drop-zone') : null;

    if (zone && zone.dataset.type === el.dataset.type) {
        playSound(true);
        el.classList.add('collected');
        collected++;

        // 語音回饋
        if (window.SpeechHelper) {
            const typeName = typeNames[el.dataset.type];
            SpeechHelper.speak(`答對了！${el.dataset.name}是${typeName}！`);
        }

        updateProgress();

        if (collected >= total) {
            setTimeout(gameWin, 800);
        }
    } else if (zone) {
        // 放錯籃子
        playSound(false);

        const correctType = typeNames[el.dataset.type];
        if (window.SpeechHelper) {
            SpeechHelper.speak(`${el.dataset.name}是${correctType}喔！再試試看！`);
        }

        el.style.position = '';
        el.style.left = '';
        el.style.top = '';
        el.style.zIndex = '';
    } else {
        // 沒放到籃子
        el.style.position = '';
        el.style.left = '';
        el.style.top = '';
        el.style.zIndex = '';
    }
}

function updateProgress() {
    const progressEl = document.getElementById('progress');
    if (progressEl) {
        progressEl.textContent = `${collected} / ${total}`;
    }

    const progressFill = document.querySelector('.progress-fill');
    if (progressFill) {
        progressFill.style.width = (collected / total * 100) + '%';
    }
}

function gameWin() {
    document.getElementById('farmer-img').classList.add('dancing');

    if (window.SoundHelper) SoundHelper.play('win');

    if (window.SpeechHelper) {
        SpeechHelper.speak('太棒了！豐收啦！農夫好開心！', () => {
            document.getElementById('message-overlay').classList.remove('hidden');
        });
    } else {
        setTimeout(() => {
            document.getElementById('message-overlay').classList.remove('hidden');
        }, 1000);
    }
}

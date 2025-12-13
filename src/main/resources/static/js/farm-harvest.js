const items = [
    { type: 'veggie', img: '/images/games/farm-harvest/carrot.png', name: '胡蘿蔔' },
    { type: 'veggie', img: '/images/games/farm-harvest/carrot.png', name: '胡蘿蔔' },
    { type: 'fruit', img: '/images/games/farm-harvest/apple.png', name: '蘋果' },
    { type: 'fruit', img: '/images/games/farm-harvest/apple.png', name: '蘋果' },
    { type: 'fruit', img: '/images/games/farm-harvest/banana.png', name: '香蕉' }
];

let collected = 0;
const total = items.length;
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    if (audioCtx.state === 'suspended') audioCtx.resume();
    init();
}

function init() {
    const area = document.getElementById('items-area');
    const shuffled = [...items].sort(() => Math.random() - 0.5);

    shuffled.forEach((item, index) => {
        const el = document.createElement('div');
        el.className = 'harvest-item';
        el.dataset.type = item.type;
        el.dataset.index = index;

        const img = document.createElement('img');
        img.src = item.img;
        el.appendChild(img);

        makeDraggable(el);
        area.appendChild(el);
    });
}

function playSound(success) {
    if (audioCtx.state === 'suspended') audioCtx.resume();
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (success) {
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(600, audioCtx.currentTime + 0.15);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
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

        initialLeft = rect.left;
        initialTop = rect.top;

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

        // Highlight drop zones
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

        if (collected >= total) {
            setTimeout(gameWin, 500);
        }
    } else {
        playSound(false);
        el.style.position = '';
        el.style.left = '';
        el.style.top = '';
        el.style.zIndex = '';
    }
}

function gameWin() {
    document.getElementById('farmer-img').classList.add('dancing');
    setTimeout(() => {
        document.getElementById('message-overlay').classList.remove('hidden');
    }, 1000);
}

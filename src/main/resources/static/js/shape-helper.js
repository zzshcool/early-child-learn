const shapes = [
    { type: 'circle', img: '/images/games/shape-helper/circle.png' },
    { type: 'triangle', img: '/images/games/shape-helper/triangle.png' },
    { type: 'square', img: '/images/games/shape-helper/square.png' }
];

let itemsProcessed = 0;
const totalItems = shapes.length;
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function init() {
    // Randomize pool maybe? For now just place them
    const pool = document.getElementById('shapes-pool');
    shapes.forEach(shape => {
        const el = document.createElement('div');
        el.className = 'draggable-shape';
        el.dataset.shape = shape.type;
        
        const img = document.createElement('img');
        img.src = shape.img;
        el.appendChild(img);
        
        makeDraggable(el);
        pool.appendChild(el);
    });
}

function playSound(success) {
    if (audioCtx.state === 'suspended') audioCtx.resume();
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    
    if (success) {
        osc.frequency.setValueAtTime(523.25, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(783.99, audioCtx.currentTime + 0.1);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.3);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.3);
    } else {
        osc.frequency.setValueAtTime(100, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.2);
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
        // Move to body for absolute positioning ensuring z-index over everything
        el.style.position = 'fixed';
        el.style.left = rect.left + 'px';
        el.style.top = rect.top + 'px';
        el.style.width = rect.width + 'px';
        
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
    };
    
    const onEnd = (e) => {
        document.removeEventListener('mousemove', onMove);
        document.removeEventListener('mouseup', onEnd);
        document.removeEventListener('touchmove', onMove);
        document.removeEventListener('touchend', onEnd);
        
        checkDrop(el);
    };
    
    el.addEventListener('mousedown', onStart);
    el.addEventListener('touchstart', onStart, { passive: false });
}

function checkDrop(el) {
    // Hide to get element below
    el.style.visibility = 'hidden';
    const rect = el.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    
    const belowElement = document.elementFromPoint(centerX, centerY);
    el.style.visibility = 'visible';
    
    const slot = belowElement ? belowElement.closest('.target-slot') : null;
    
    if (slot && slot.dataset.shape === el.dataset.shape) {
        // Match!
        playSound(true);
        // Snap to slot center? Or just hide and show filled slot
        // For visual, let's clone image into slot and remove dragger
        const clone = el.querySelector('img').cloneNode(true);
        clone.style.width = '80px';
        clone.style.height = '80px';
        slot.innerHTML = ''; // Remove outline
        slot.appendChild(clone);
        
        el.remove();
        itemsProcessed++;
        
        if (itemsProcessed >= totalItems) {
            setTimeout(() => {
                document.getElementById('message-overlay').classList.remove('hidden');
            }, 500);
        }
    } else {
        // Wrong or miss
        playSound(false);
        // Reset position (simple: remove fixed positioning, let it flow back to pool)
        el.style.position = '';
        el.style.left = '';
        el.style.top = '';
        el.style.width = '';
    }
}

init();

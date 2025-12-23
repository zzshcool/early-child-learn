/**
 * 兒童遊戲輔助系統
 * 統一管理遊戲中的語音、音效、動畫等功能
 * 專為 4-6 歲幼兒設計
 */

// ========================================
// 語音朗讀系統
// ========================================
// 避免重複宣告（可能已由 audio-manager.js 定義）
const GameAudio = window.GameAudio || {
    enabled: true,
    rate: 0.85,
    pitch: 1.15,
    lang: 'zh-TW',

    /**
     * 朗讀文字
     */
    speak(text, lang = 'zh-TW', callback) {
        if (!this.enabled || !('speechSynthesis' in window)) {
            if (callback) callback();
            return;
        }
        speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = lang;
        utterance.rate = this.rate;
        utterance.pitch = this.pitch;
        utterance.volume = 1;
        if (callback) utterance.onend = callback;
        speechSynthesis.speak(utterance);
    },

    /**
     * 正確答案回饋
     */
    correct(callback) {
        const phrases = [
            '太棒了！答對了！',
            '很好！你好聰明！',
            '答對了！繼續加油！',
            '好厲害！'
        ];
        this.speak(phrases[Math.floor(Math.random() * phrases.length)], 'zh-TW', callback);
    },

    /**
     * 錯誤答案回饋（正向鼓勵）
     */
    tryAgain(callback) {
        const phrases = [
            '再試一次喔～',
            '沒關係，再試試看！',
            '加油！你可以的！'
        ];
        this.speak(phrases[Math.floor(Math.random() * phrases.length)], 'zh-TW', callback);
    },

    /**
     * 遊戲勝利
     */
    win(callback) {
        this.speak('恭喜你！你好棒棒！', 'zh-TW', callback);
    }
};

// ========================================
// 音效系統
// ========================================
// 避免重複宣告（可能已由 audio-manager.js 定義）
const GameSound = window.GameSound || {
    audioCtx: null,
    enabled: true,

    init() {
        if (!this.audioCtx) {
            this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
    },

    play(type) {
        if (!this.enabled) return;
        this.init();
        switch (type) {
            case 'click': this.playTone(800, 0.1, 'sine'); break;
            case 'correct': this.playMelody([523, 659, 784], 0.15, 'sine'); break;
            case 'wrong': this.playTone(200, 0.3, 'sawtooth'); break;
            case 'pop': this.playTone(600, 0.1, 'sine', 900); break;
            case 'win': this.playMelody([523, 587, 659, 698, 784, 880], 0.12, 'triangle'); break;
            case 'star': this.playMelody([784, 988, 1175], 0.1, 'sine'); break;
            default: this.playTone(400, 0.1, 'sine');
        }
    },

    playTone(freq, duration, type, endFreq) {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        osc.type = type || 'sine';
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime);
        if (endFreq) osc.frequency.linearRampToValueAtTime(endFreq, this.audioCtx.currentTime + duration);
        gain.gain.setValueAtTime(0.2, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + duration);
        osc.start();
        osc.stop(this.audioCtx.currentTime + duration);
    },

    playMelody(notes, noteDuration, type) {
        notes.forEach((freq, i) => {
            setTimeout(() => this.playTone(freq, noteDuration, type), i * noteDuration * 1000);
        });
    }
};

// ========================================
// 動畫與特效系統
// ========================================
const GameEffects = {
    /**
     * 創建慶祝紙屑效果
     */
    createConfetti(container, count = 30) {
        const colors = ['#FF6B6B', '#4ECDC4', '#FFE66D', '#95E1D3', '#F38181', '#AA96DA'];
        for (let i = 0; i < count; i++) {
            const confetti = document.createElement('div');
            confetti.className = 'confetti';
            confetti.style.cssText = `
                position: fixed;
                width: 12px;
                height: 12px;
                top: -20px;
                left: ${Math.random() * 100}%;
                background: ${colors[Math.floor(Math.random() * colors.length)]};
                animation: confetti-fall ${2 + Math.random() * 2}s linear forwards;
                animation-delay: ${Math.random() * 2}s;
                border-radius: 50%;
                z-index: 1000;
            `;
            (container || document.body).appendChild(confetti);
            setTimeout(() => confetti.remove(), 4000);
        }
    },

    /**
     * 創建星星飄落效果
     */
    createStars(container, count = 20) {
        const starEmojis = ['⭐', '🌟', '✨', '💫', '🎉', '🎊'];
        for (let i = 0; i < count; i++) {
            const star = document.createElement('div');
            star.className = 'floating-star';
            star.textContent = starEmojis[Math.floor(Math.random() * starEmojis.length)];
            star.style.cssText = `
                position: fixed;
                left: ${Math.random() * 100}%;
                top: ${Math.random() * 100}%;
                font-size: 2rem;
                animation: star-float 1.5s ease-out forwards;
                animation-delay: ${Math.random() * 0.5}s;
                z-index: 1001;
                pointer-events: none;
            `;
            (container || document.body).appendChild(star);
            setTimeout(() => star.remove(), 2000);
        }
    },

    /**
     * 顯示成功覆蓋層
     */
    showSuccessOverlay(message = '太棒了！', duration = 2500) {
        let overlay = document.getElementById('game-success-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'game-success-overlay';
            overlay.innerHTML = `
                <div class="success-content">
                    <div class="success-emoji">🎉</div>
                    <div class="success-text">${message}</div>
                </div>
            `;
            overlay.style.cssText = `
                display: flex;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                justify-content: center;
                align-items: center;
                z-index: 1000;
                animation: fadeIn 0.3s ease;
            `;
            document.body.appendChild(overlay);
        } else {
            overlay.querySelector('.success-text').textContent = message;
            overlay.style.display = 'flex';
        }

        this.createStars();
        GameSound.play('win');
        GameAudio.speak(message);

        setTimeout(() => {
            overlay.style.display = 'none';
        }, duration);
    }
};

// ========================================
// 通用工具函數
// ========================================
const GameUtils = {
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
    },

    /**
     * 隨機選取 n 個元素
     */
    pickRandom(array, n) {
        return this.shuffle(array).slice(0, n);
    },

    /**
     * 延遲執行
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
};

// ========================================
// 添加必要的 CSS 動畫
// ========================================
(function addGameStyles() {
    if (document.getElementById('game-helper-styles')) return;
    const style = document.createElement('style');
    style.id = 'game-helper-styles';
    style.textContent = `
        @keyframes confetti-fall {
            0% { transform: translateY(0) rotate(0deg); opacity: 1; }
            100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
        }
        @keyframes star-float {
            0% { transform: translateY(0) scale(0) rotate(0deg); opacity: 1; }
            100% { transform: translateY(-100px) scale(1) rotate(360deg); opacity: 0; }
        }
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        #game-success-overlay .success-content {
            text-align: center;
            animation: bounceIn 0.5s ease;
        }
        #game-success-overlay .success-emoji {
            font-size: 6rem;
            animation: pulse 0.5s ease infinite alternate;
        }
        #game-success-overlay .success-text {
            font-size: 2rem;
            color: white;
            margin-top: 1rem;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
        }
        @keyframes bounceIn {
            0% { transform: scale(0); opacity: 0; }
            50% { transform: scale(1.2); }
            100% { transform: scale(1); opacity: 1; }
        }
        @keyframes pulse {
            from { transform: scale(1); }
            to { transform: scale(1.1); }
        }
    `;
    document.head.appendChild(style);
})();

// 全域匯出
window.GameAudio = GameAudio;
window.GameSound = GameSound;
window.GameEffects = GameEffects;
window.GameUtils = GameUtils;

// 相容舊版 API
window.playGameSound = (text, lang) => GameAudio.speak(text, lang);
window.speak = (text, lang) => GameAudio.speak(text, lang);

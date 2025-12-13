/**
 * 兒童語音輔助系統
 * 專為 3-6 歲幼兒設計的語音回饋
 * 使用 Web Speech API (SpeechSynthesis)
 */
const SpeechHelper = {
    enabled: true,
    rate: 0.85,  // 較慢語速適合幼兒
    pitch: 1.15, // 稍高音調較活潑
    lang: 'zh-TW',

    /**
     * 朗讀文字
     * @param {string} text - 要朗讀的文字
     * @param {function} callback - 朗讀完成後的回調
     */
    speak: function (text, callback) {
        if (!this.enabled || !('speechSynthesis' in window)) {
            if (callback) callback();
            return;
        }

        // 取消之前的語音
        speechSynthesis.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = this.lang;
        utterance.rate = this.rate;
        utterance.pitch = this.pitch;
        utterance.volume = 1;

        if (callback) {
            utterance.onend = callback;
        }

        speechSynthesis.speak(utterance);
    },

    /**
     * 正確答案回饋
     */
    correct: function (callback) {
        const phrases = [
            '太棒了！答對了！',
            '很好！你好聰明！',
            '答對了！繼續加油！',
            '好厲害！'
        ];
        const phrase = phrases[Math.floor(Math.random() * phrases.length)];
        this.speak(phrase, callback);
    },

    /**
     * 錯誤答案回饋（正向鼓勵）
     */
    tryAgain: function (callback) {
        const phrases = [
            '再試一次喔！',
            '沒關係，再試試看！',
            '加油！你可以的！'
        ];
        const phrase = phrases[Math.floor(Math.random() * phrases.length)];
        this.speak(phrase, callback);
    },

    /**
     * 勝利回饋
     */
    win: function (callback) {
        this.speak('恭喜你！你好棒棒！', callback);
    },

    /**
     * 遊戲開始
     */
    gameStart: function (callback) {
        this.speak('準備好了嗎？遊戲開始！', callback);
    },

    /**
     * 過關回饋
     */
    levelComplete: function (callback) {
        this.speak('太厲害了！過關了！', callback);
    },

    /**
     * 時間到
     */
    timeUp: function (callback) {
        this.speak('時間到了！再玩一次吧！', callback);
    }
};

/**
 * 通用音效系統
 * 使用 Web Audio API 產生音效
 */
const SoundHelper = {
    audioCtx: null,
    enabled: true,

    init: function () {
        if (!this.audioCtx) {
            this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        if (this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
    },

    /**
     * 播放音效
     * @param {string} type - 音效類型
     */
    play: function (type) {
        if (!this.enabled) return;
        this.init();

        switch (type) {
            case 'click':
                this.playTone(800, 0.1, 'sine');
                break;
            case 'correct':
                this.playMelody([523, 659, 784], 0.15, 'sine');
                break;
            case 'wrong':
                this.playTone(200, 0.3, 'sawtooth');
                break;
            case 'pop':
                this.playTone(600, 0.1, 'sine', 900);
                break;
            case 'win':
                this.playMelody([523, 587, 659, 698, 784, 880], 0.12, 'triangle');
                break;
            case 'levelUp':
                this.playMelody([440, 554, 659], 0.2, 'sine');
                break;
            case 'countdown':
                this.playTone(500, 0.15, 'sine');
                break;
            case 'go':
                this.playTone(800, 0.2, 'sine');
                break;
            default:
                this.playTone(400, 0.1, 'sine');
        }
    },

    /**
     * 播放單音
     */
    playTone: function (freq, duration, type, endFreq) {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.type = type || 'sine';
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime);
        if (endFreq) {
            osc.frequency.linearRampToValueAtTime(endFreq, this.audioCtx.currentTime + duration);
        }

        gain.gain.setValueAtTime(0.2, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.01, this.audioCtx.currentTime + duration);

        osc.start();
        osc.stop(this.audioCtx.currentTime + duration);
    },

    /**
     * 播放旋律
     */
    playMelody: function (notes, noteDuration, type) {
        notes.forEach((freq, i) => {
            setTimeout(() => {
                this.playTone(freq, noteDuration, type);
            }, i * noteDuration * 1000);
        });
    }
};

// 全域可用
window.SpeechHelper = SpeechHelper;
window.SoundHelper = SoundHelper;

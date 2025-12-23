/**
 * AudioManager - 全站音效與語音統一管理器
 * 提供 TTS、音效播放、音量控制等功能
 */
class AudioManager {
    constructor() {
        this.enabled = this.loadPreference('audioEnabled', true);
        this.volume = this.loadPreference('audioVolume', 0.7);
        this.speechRate = 0.9;
        this.speechLang = 'zh-TW';
        this.synth = window.speechSynthesis;

        // 音效快取
        this.soundCache = {};

        // 預設音效定義
        this.soundDefs = {
            click: { freq: 800, duration: 0.1, type: 'sine' },
            correct: { freq: 600, duration: 0.2, type: 'sine', sweep: 900 },
            wrong: { freq: 200, duration: 0.3, type: 'sawtooth' },
            pop: { freq: 400, duration: 0.15, type: 'sine', sweep: 600 },
            win: { notes: [400, 500, 600, 800], duration: 0.2 }
        };

        // 初始化 AudioContext
        this.initAudioContext();
    }

    initAudioContext() {
        this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();

        // 用戶互動後恢復 AudioContext
        const resumeAudio = () => {
            if (this.audioCtx.state === 'suspended') {
                this.audioCtx.resume();
            }
        };
        document.addEventListener('click', resumeAudio, { once: true });
        document.addEventListener('touchstart', resumeAudio, { once: true });
    }

    // === 偏好設定 ===

    loadPreference(key, defaultValue) {
        try {
            const stored = localStorage.getItem(`audioManager_${key}`);
            return stored !== null ? JSON.parse(stored) : defaultValue;
        } catch {
            return defaultValue;
        }
    }

    savePreference(key, value) {
        try {
            localStorage.setItem(`audioManager_${key}`, JSON.stringify(value));
        } catch { }
    }

    setEnabled(enabled) {
        this.enabled = enabled;
        this.savePreference('audioEnabled', enabled);
        if (!enabled) {
            this.synth.cancel();
        }
    }

    setVolume(volume) {
        this.volume = Math.max(0, Math.min(1, volume));
        this.savePreference('audioVolume', this.volume);
    }

    // === 音效播放 ===

    play(soundName) {
        if (!this.enabled) return;

        const def = this.soundDefs[soundName];
        if (!def) return;

        if (def.notes) {
            // 多音符序列（如 win）
            def.notes.forEach((freq, i) => {
                this.playTone(freq, def.duration, 'sine', i * def.duration);
            });
        } else {
            this.playTone(def.freq, def.duration, def.type, 0, def.sweep);
        }
    }

    playTone(freq, duration, type = 'sine', delay = 0, sweepTo = null) {
        if (!this.enabled || !this.audioCtx) return;

        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();

        osc.connect(gain);
        gain.connect(this.audioCtx.destination);

        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.audioCtx.currentTime + delay);

        if (sweepTo) {
            osc.frequency.linearRampToValueAtTime(sweepTo, this.audioCtx.currentTime + delay + duration);
        }

        gain.gain.setValueAtTime(this.volume * 0.2, this.audioCtx.currentTime + delay);
        gain.gain.linearRampToValueAtTime(0, this.audioCtx.currentTime + delay + duration);

        osc.start(this.audioCtx.currentTime + delay);
        osc.stop(this.audioCtx.currentTime + delay + duration);
    }

    // === 語音播放 ===

    speak(text, callback = null) {
        if (!this.enabled || !this.synth) {
            if (callback) callback();
            return;
        }

        // 取消之前的語音
        this.synth.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = this.speechLang;
        utterance.rate = this.speechRate;
        utterance.volume = this.volume;

        if (callback) {
            utterance.onend = callback;
            utterance.onerror = callback;
        }

        this.synth.speak(utterance);
    }

    // 快捷語音
    correct() {
        this.speak('答對了！好棒！');
    }

    tryAgain() {
        this.speak('再試試看喔！');
    }

    welcome(gameName) {
        this.speak(`歡迎來到${gameName}！`);
    }

    // === 靜態實例 ===

    static getInstance() {
        if (!AudioManager._instance) {
            AudioManager._instance = new AudioManager();
        }
        return AudioManager._instance;
    }
}

// 全局實例
const audioManager = AudioManager.getInstance();

// 相容性別名（向後兼容，避免重複宣告）
const GameAudio = window.GameAudio || {
    speak: (text, callback) => audioManager.speak(text, callback),
    correct: () => audioManager.correct(),
    tryAgain: () => audioManager.tryAgain(),
    setEnabled: (enabled) => audioManager.setEnabled(enabled),
    setVolume: (volume) => audioManager.setVolume(volume)
};

const GameSound = window.GameSound || {
    play: (soundName) => audioManager.play(soundName)
};

// 確保全域可用
window.GameAudio = GameAudio;
window.GameSound = GameSound;
window.audioManager = audioManager;

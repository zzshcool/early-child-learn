/**
 * 恐龍吃水果 - 遊戲引擎
 * 專為 3-6 歲幼兒設計，包含語音朗讀
 */
const fruits = [
    { name: 'apple', img: '/images/games/dino-fruit/apple.png', label: '蘋果', emoji: '🍎' },
    { name: 'banana', img: '/images/games/dino-fruit/banana.png', label: '香蕉', emoji: '🍌' },
    { name: 'grape', img: '/images/games/dino-fruit/grape.png', label: '葡萄', emoji: '🍇' },
    { name: 'orange', img: '/images/games/dino-fruit/orange.png', label: '橘子', emoji: '🍊' }
];

let currentRequest = null;
let score = 0;
const totalRounds = 5;
const dinosaur = document.getElementById('dino-img');
const bubble = document.getElementById('bubble');
const fruitsContainer = document.getElementById('fruits-container');

// 音效系統
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();

function startGame() {
    document.getElementById('tutorial-overlay').classList.add('hidden');
    if (audioCtx.state === 'suspended') audioCtx.resume();

    // 語音說明
    if (window.SpeechHelper) {
        SpeechHelper.speak('點擊恐龍想吃的水果！', nextRound);
    } else {
        nextRound();
    }
}

function playSound(type) {
    if (window.SoundHelper) {
        switch (type) {
            case 'eat': SoundHelper.play('correct'); break;
            case 'wrong': SoundHelper.play('wrong'); break;
            case 'end': SoundHelper.play('win'); break;
            default: SoundHelper.play('click');
        }
        return;
    }

    // 備用音效
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.connect(gain);
    gain.connect(audioCtx.destination);

    if (type === 'eat') {
        osc.frequency.setValueAtTime(400, audioCtx.currentTime);
        osc.frequency.linearRampToValueAtTime(600, audioCtx.currentTime + 0.2);
        gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0, audioCtx.currentTime + 0.3);
        osc.start();
        osc.stop(audioCtx.currentTime + 0.3);
    } else if (type === 'wrong') {
        osc.frequency.setValueAtTime(150, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
        osc.type = 'sawtooth';
        osc.start();
        osc.stop(audioCtx.currentTime + 0.3);
    } else if (type === 'end') {
        const notes = [400, 500, 600, 800];
        notes.forEach((f, i) => {
            const o = audioCtx.createOscillator();
            const g = audioCtx.createGain();
            o.connect(g);
            g.connect(audioCtx.destination);
            o.frequency.value = f;
            g.gain.value = 0.15;
            g.gain.linearRampToValueAtTime(0, audioCtx.currentTime + i * 0.2 + 0.3);
            o.start(audioCtx.currentTime + i * 0.2);
            o.stop(audioCtx.currentTime + i * 0.2 + 0.3);
        });
    }
}

function nextRound() {
    if (score >= totalRounds) {
        playSound('end');

        if (window.SpeechHelper) {
            SpeechHelper.speak('太棒了！恐龍吃飽了！', () => {
                document.getElementById('message-overlay').classList.remove('hidden');
            });
        } else {
            document.getElementById('message-overlay').classList.remove('hidden');
        }
        return;
    }

    currentRequest = fruits[Math.floor(Math.random() * fruits.length)];

    // 更新對話框
    bubble.textContent = `我想吃${currentRequest.label}! ${currentRequest.emoji}`;
    bubble.classList.remove('hidden');

    // 語音說出恐龍想吃什麼
    if (window.SpeechHelper) {
        SpeechHelper.speak(`我想吃${currentRequest.label}`);
    }

    // 更新進度
    updateProgress();

    renderOptions();
}

function updateProgress() {
    const progressEl = document.getElementById('progress');
    if (progressEl) {
        progressEl.textContent = `${score} / ${totalRounds}`;
    }

    const progressFill = document.querySelector('.progress-fill');
    if (progressFill) {
        progressFill.style.width = (score / totalRounds * 100) + '%';
    }
}

function renderOptions() {
    fruitsContainer.innerHTML = '';
    // 隨機排列選項
    const options = [...fruits].sort(() => Math.random() - 0.5);

    options.forEach(fruit => {
        const div = document.createElement('div');
        div.className = 'fruit-option';

        const img = document.createElement('img');
        img.src = fruit.img;
        img.alt = fruit.label;
        div.appendChild(img);

        // 水果名稱標籤
        const label = document.createElement('div');
        label.className = 'fruit-label';
        label.textContent = fruit.label;
        div.appendChild(label);

        div.onclick = () => checkAnswer(fruit);
        fruitsContainer.appendChild(div);
    });
}

function checkAnswer(fruit) {
    // 播放點擊音效
    if (window.SoundHelper) SoundHelper.play('click');

    if (fruit.name === currentRequest.name) {
        playSound('eat');
        dinosaur.classList.add('eating');
        bubble.classList.add('hidden');
        score++;

        // 語音回饋
        if (window.SpeechHelper) {
            SpeechHelper.correct();
        }

        setTimeout(() => {
            dinosaur.classList.remove('eating');
            nextRound();
        }, 1200);
    } else {
        playSound('wrong');
        dinosaur.classList.add('shake');

        // 語音提示正確答案
        if (window.SpeechHelper) {
            SpeechHelper.speak(`不對喔，這是${fruit.label}！再找找${currentRequest.label}在哪裡？`);
        }

        setTimeout(() => dinosaur.classList.remove('shake'), 500);
    }
}

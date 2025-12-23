/**
 * 筆畫練習共用 JavaScript 模塊
 * 適用於 numbers/stroke.html, english/stroke.html, bopomofo/stroke.html
 */

const StrokePractice = (function () {
    // 私有變數
    let canvas = null;
    let ctx = null;
    let isDrawing = false;
    let hasDrawn = false;
    let currentIndex = 0;
    let chars = [];
    let charType = 'numbers'; // 'numbers', 'english', 'bopomofo'
    let onSuccessCallback = null;

    /**
     * 初始化筆畫練習模塊
     * @param {Object} options - 設定選項
     * @param {string} options.charType - 字符類型 ('numbers', 'english', 'bopomofo')
     * @param {Array} options.chars - 字符陣列
     * @param {number} options.startIndex - 起始索引
     * @param {Function} options.onSuccess - 成功回調
     */
    function init(options = {}) {
        charType = options.charType || 'numbers';
        chars = options.chars || [];
        currentIndex = options.startIndex || 0;
        onSuccessCallback = options.onSuccess || null;

        canvas = document.getElementById('drawing-canvas');
        if (!canvas) {
            console.error('StrokePractice: 找不到 drawing-canvas 元素');
            return;
        }

        ctx = canvas.getContext('2d');
        ctx.lineWidth = 15;
        ctx.lineCap = 'round';
        ctx.strokeStyle = '#333';

        setupEventListeners();

        // 初始化獎勵系統
        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.createStarDisplay();
        }
    }

    /**
     * 設置事件監聽器
     */
    function setupEventListeners() {
        canvas.addEventListener('mousedown', startDraw);
        canvas.addEventListener('mouseup', endDraw);
        canvas.addEventListener('mousemove', draw);
        canvas.addEventListener('touchstart', startDraw);
        canvas.addEventListener('touchend', endDraw);
        canvas.addEventListener('touchmove', draw);
    }

    /**
     * 開始繪製
     */
    function startDraw(e) {
        isDrawing = true;
        hasDrawn = true;
        draw(e);
    }

    /**
     * 結束繪製
     */
    function endDraw() {
        isDrawing = false;
        ctx.beginPath();
    }

    /**
     * 繪製
     */
    function draw(e) {
        if (!isDrawing) return;
        e.preventDefault();

        const rect = canvas.getBoundingClientRect();
        const scaleX = canvas.width / rect.width;
        const scaleY = canvas.height / rect.height;

        const clientX = (e.clientX || e.touches[0].clientX);
        const clientY = (e.clientY || e.touches[0].clientY);

        const x = (clientX - rect.left) * scaleX;
        const y = (clientY - rect.top) * scaleY;

        ctx.lineTo(x, y);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(x, y);
    }

    /**
     * 清除畫布
     */
    function clearCanvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        hasDrawn = false;
        updateSimilarityDisplay('開始練習吧！', 0);
    }

    /**
     * 更新相似度顯示
     */
    function updateSimilarityDisplay(text, percentage) {
        const textEl = document.getElementById('similarity-text');
        const fillEl = document.getElementById('similarity-fill');
        if (textEl) textEl.textContent = text;
        if (fillEl) fillEl.style.width = percentage + '%';
    }

    /**
     * 從圖像數據中提取像素
     */
    function getBlackPixels(imageData) {
        const pixels = [];
        const data = imageData.data;
        for (let y = 0; y < imageData.height; y++) {
            for (let x = 0; x < imageData.width; x++) {
                const idx = (y * imageData.width + x) * 4;
                if (data[idx + 3] > 50) {
                    pixels.push({ x, y });
                }
            }
        }
        return pixels;
    }

    /**
     * 獲取引導路徑像素（使用 SVG 路徑）
     */
    function getGuidePixelsFromPaths(paths) {
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = 300;
        tempCanvas.height = 300;
        const tempCtx = tempCanvas.getContext('2d');

        tempCtx.strokeStyle = '#000';
        tempCtx.lineWidth = 20;
        tempCtx.lineCap = 'round';
        tempCtx.lineJoin = 'round';

        paths.forEach(pathData => {
            const path = new Path2D(pathData);
            tempCtx.stroke(path);
        });

        const imageData = tempCtx.getImageData(0, 0, tempCanvas.width, tempCanvas.height);
        return getBlackPixels(imageData);
    }

    /**
     * 獲取引導像素（使用字符渲染）
     */
    function getGuidePixelsFromChar(char) {
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = 300;
        tempCanvas.height = 300;
        const tempCtx = tempCanvas.getContext('2d');

        tempCtx.fillStyle = '#000';
        tempCtx.font = '200px "KaiTi", "BiauKai", "Microsoft JhengHei", sans-serif';
        tempCtx.textAlign = 'center';
        tempCtx.textBaseline = 'middle';
        tempCtx.fillText(char, 150, 150);

        const imageData = tempCtx.getImageData(0, 0, tempCanvas.width, tempCanvas.height);
        return getBlackPixels(imageData);
    }

    /**
     * 計算相似度
     */
    function calculateSimilarity(userPixels, guidePixels) {
        if (userPixels.length === 0 || guidePixels.length === 0) {
            return 0;
        }

        const guideSet = new Set();
        guidePixels.forEach(p => guideSet.add(`${Math.floor(p.x / 3)},${Math.floor(p.y / 3)}`));

        let matchCount = 0;
        const userSet = new Set();
        userPixels.forEach(p => {
            const key = `${Math.floor(p.x / 3)},${Math.floor(p.y / 3)}`;
            userSet.add(key);
            if (guideSet.has(key)) {
                matchCount++;
            }
        });

        let coverCount = 0;
        guidePixels.forEach(p => {
            const key = `${Math.floor(p.x / 3)},${Math.floor(p.y / 3)}`;
            if (userSet.has(key)) {
                coverCount++;
            }
        });

        const accuracy = userPixels.length > 0 ? matchCount / userPixels.length : 0;
        const coverage = guidePixels.length > 0 ? coverCount / guidePixels.length : 0;

        return (accuracy * 0.5 + coverage * 0.5);
    }

    /**
     * 檢查相似度
     * @param {Array|string} guide - 路徑陣列或字符
     * @param {boolean} usePaths - 是否使用 SVG 路徑
     */
    function checkSimilarity(guide, usePaths = true) {
        if (!hasDrawn) {
            updateSimilarityDisplay('請先練習寫字！', 0);
            return;
        }

        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const userPixels = getBlackPixels(imageData);
        const guidePixels = usePaths
            ? getGuidePixelsFromPaths(guide)
            : getGuidePixelsFromChar(guide);

        const similarity = calculateSimilarity(userPixels, guidePixels);
        const percentage = Math.round(similarity * 100);

        updateSimilarityDisplay('', percentage);

        if (percentage >= 50) {
            updateSimilarityDisplay(`太棒了！寫得很好！${percentage}% 🌟`, percentage);
            showSuccessAnimation();
        } else if (percentage >= 30) {
            updateSimilarityDisplay(`很好喔！繼續加油！${percentage}% 👍`, percentage);
        } else {
            updateSimilarityDisplay(`再試試看，你一定可以的！${percentage}% 💪`, percentage);
        }
    }

    /**
     * 顯示成功動畫
     */
    function showSuccessAnimation() {
        const overlay = document.getElementById('success-overlay');
        const starsContainer = document.getElementById('stars-container');

        // 播放音效
        if (typeof GameAudio !== 'undefined') GameAudio.correct();
        if (typeof GameSound !== 'undefined') GameSound.play('win');

        // 獎勵星星
        if (typeof RewardSystem !== 'undefined') {
            RewardSystem.addStars(2);
            RewardSystem.recordGameComplete(charType);
        }

        // 顯示動畫
        if (overlay) overlay.classList.add('show');

        // 星星動畫
        if (starsContainer) {
            const starEmojis = ['⭐', '🌟', '✨', '💫', '🎉', '🎊'];
            for (let i = 0; i < 20; i++) {
                const star = document.createElement('div');
                star.className = 'star';
                star.textContent = starEmojis[Math.floor(Math.random() * starEmojis.length)];
                star.style.left = Math.random() * 100 + '%';
                star.style.top = Math.random() * 100 + '%';
                star.style.animationDelay = Math.random() * 0.5 + 's';
                starsContainer.appendChild(star);
            }
        }

        // 延遲後隱藏並觸發回調
        setTimeout(() => {
            if (overlay) overlay.classList.remove('show');
            if (starsContainer) starsContainer.innerHTML = '';
            if (onSuccessCallback) onSuccessCallback();
        }, 2500);
    }

    /**
     * 渲染 SVG 筆畫指引
     */
    function renderGuide(type, char) {
        const group = document.getElementById('paths-group');
        if (!group) return;

        group.innerHTML = '';

        if (typeof getStrokePaths !== 'function') {
            console.warn('StrokePractice: getStrokePaths 函數未定義');
            return [];
        }

        const paths = getStrokePaths(type, char);

        paths.forEach((d, index) => {
            const bgPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
            bgPath.setAttribute("d", d);
            bgPath.setAttribute("class", "guide-path");
            group.appendChild(bgPath);

            const animPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
            animPath.setAttribute("d", d);
            animPath.setAttribute("class", "anim-path");
            animPath.setAttribute("marker-end", "url(#arrow)");
            animPath.style.animationDelay = `${index * 1}s`;
            group.appendChild(animPath);
        });

        return paths;
    }

    // 公開 API
    return {
        init,
        clearCanvas,
        checkSimilarity,
        renderGuide,
        showSuccessAnimation,
        getBlackPixels,
        calculateSimilarity,
        getGuidePixelsFromPaths,
        getGuidePixelsFromChar,

        // Getters
        get currentIndex() { return currentIndex; },
        set currentIndex(val) { currentIndex = val; },
        get chars() { return chars; },
        get hasDrawn() { return hasDrawn; }
    };
})();

// 向後兼容：將部分函數暴露到全局
if (typeof window !== 'undefined') {
    window.StrokePractice = StrokePractice;
}

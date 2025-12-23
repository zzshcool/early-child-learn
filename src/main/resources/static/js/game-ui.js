/**
 * GameUI - 遊戲 UI 統一管理模組
 * 提供導航列、回饋效果、對話框等共用 UI 功能
 */
const GameUI = {
    /**
     * 創建統一頂部導航列
     */
    createHeader(title, options = {}) {
        const {
            backUrl = '/',
            showHome = true,
            showStars = true
        } = options;

        const existing = document.querySelector('.game-header');
        if (existing) existing.remove();

        const header = document.createElement('div');
        header.className = 'game-header';
        header.innerHTML = `
            <div class="game-header-left">
                <a href="${backUrl}" class="header-btn" title="返回" aria-label="返回上一頁">←</a>
            </div>
            <div class="game-header-title">${title}</div>
            <div class="game-header-right">
                ${showHome ? '<a href="/" class="header-btn" title="首頁" aria-label="回首頁">🏠</a>' : ''}
                ${showStars ? '<div id="header-stars" class="header-btn" style="cursor: pointer;">⭐</div>' : ''}
            </div>
        `;
        document.body.insertBefore(header, document.body.firstChild);

        // 星星顯示
        if (showStars && typeof RewardSystem !== 'undefined') {
            this.updateStarsDisplay();
            document.getElementById('header-stars').onclick = () => RewardSystem.showRewardPanel();
        }

        return header;
    },

    /**
     * 更新星星顯示
     */
    updateStarsDisplay() {
        const el = document.getElementById('header-stars');
        if (el && typeof RewardSystem !== 'undefined') {
            el.textContent = `⭐ ${RewardSystem.getStars()}`;
        }
    },

    /**
     * 創建底部工具列
     */
    createFooter(buttons = []) {
        const existing = document.querySelector('.game-footer');
        if (existing) existing.remove();

        const defaultButtons = [
            { icon: '🔊', label: '音效', action: () => this.toggleSound() },
            { icon: '❓', label: '幫助', action: () => this.showHelp() },
            { icon: '🔄', label: '重玩', action: () => location.reload() }
        ];

        const finalButtons = buttons.length > 0 ? buttons : defaultButtons;

        const footer = document.createElement('div');
        footer.className = 'game-footer';

        finalButtons.forEach(btn => {
            const button = document.createElement('button');
            button.className = 'footer-btn';
            button.innerHTML = `
                <span class="icon">${btn.icon}</span>
                <span class="label">${btn.label}</span>
            `;
            button.onclick = btn.action;
            footer.appendChild(button);
        });

        document.body.appendChild(footer);
        return footer;
    },

    /**
     * 顯示成功回饋
     */
    showSuccess(element, message = '') {
        if (element) {
            element.classList.add('feedback-success');
            setTimeout(() => element.classList.remove('feedback-success'), 500);
        }

        // 星星爆發效果
        this.createStarBurst();

        // 語音回饋
        if (message && typeof GameAudio !== 'undefined') {
            GameAudio.speak(message);
        }

        // 音效
        if (typeof GameSound !== 'undefined') {
            GameSound.play('correct');
        }
    },

    /**
     * 顯示錯誤回饋
     */
    showError(element, message = '') {
        if (element) {
            element.classList.add('feedback-error');
            setTimeout(() => element.classList.remove('feedback-error'), 600);
        }

        // 語音鼓勵
        if (message && typeof GameAudio !== 'undefined') {
            GameAudio.speak(message);
        }

        // 音效
        if (typeof GameSound !== 'undefined') {
            GameSound.play('wrong');
        }
    },

    /**
     * 創建星星爆發效果
     */
    createStarBurst(x, y) {
        const stars = ['⭐', '🌟', '✨', '💫'];
        const count = 5;

        for (let i = 0; i < count; i++) {
            const star = document.createElement('div');
            star.className = 'star-burst';
            star.textContent = stars[Math.floor(Math.random() * stars.length)];
            star.style.left = (x || window.innerWidth / 2) + (Math.random() - 0.5) * 100 + 'px';
            star.style.top = (y || window.innerHeight / 2) + (Math.random() - 0.5) * 100 + 'px';
            document.body.appendChild(star);

            setTimeout(() => star.remove(), 1000);
        }
    },

    /**
     * 顯示載入畫面
     */
    showLoading(message = '載入中', character = '🐻') {
        let overlay = document.getElementById('game-loading-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'game-loading-overlay';
            overlay.className = 'loading-overlay';
            document.body.appendChild(overlay);
        }

        overlay.innerHTML = `
            <div class="loading-character">${character}</div>
            <div class="loading-text">${message}<span class="loading-dots"></span></div>
        `;
        overlay.style.display = 'flex';

        return overlay;
    },

    /**
     * 隱藏載入畫面
     */
    hideLoading() {
        const overlay = document.getElementById('game-loading-overlay');
        if (overlay) {
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.3s';
            setTimeout(() => overlay.style.display = 'none', 300);
        }
    },

    /**
     * 顯示確認對話框
     */
    showConfirm(message, onConfirm, onCancel, icon = '❓') {
        const dialog = document.createElement('div');
        dialog.className = 'confirm-dialog';
        dialog.innerHTML = `
            <div class="confirm-content">
                <div class="confirm-icon">${icon}</div>
                <div class="confirm-message">${message}</div>
                <div class="confirm-buttons">
                    <button class="confirm-btn confirm-btn-yes">確定</button>
                    <button class="confirm-btn confirm-btn-no">取消</button>
                </div>
            </div>
        `;

        document.body.appendChild(dialog);

        dialog.querySelector('.confirm-btn-yes').onclick = () => {
            dialog.remove();
            if (onConfirm) onConfirm();
        };

        dialog.querySelector('.confirm-btn-no').onclick = () => {
            dialog.remove();
            if (onCancel) onCancel();
        };

        return dialog;
    },

    /**
     * 顯示提示氣泡
     */
    showTooltip(element, message, duration = 3000) {
        const existing = document.querySelector('.tooltip-bubble');
        if (existing) existing.remove();

        const rect = element.getBoundingClientRect();
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip-bubble';
        tooltip.textContent = message;
        tooltip.style.left = rect.left + rect.width / 2 - 100 + 'px';
        tooltip.style.top = rect.top - 50 + 'px';

        document.body.appendChild(tooltip);

        if (duration > 0) {
            setTimeout(() => tooltip.remove(), duration);
        }

        return tooltip;
    },

    /**
     * 切換音效
     */
    toggleSound() {
        if (typeof audioManager !== 'undefined') {
            const enabled = !audioManager.enabled;
            audioManager.setEnabled(enabled);
            this.showTooltip(
                document.querySelector('.footer-btn'),
                enabled ? '音效已開啟 🔊' : '音效已關閉 🔇',
                1500
            );
        }
    },

    /**
     * 顯示幫助
     */
    showHelp() {
        const helpText = document.querySelector('[data-help]')?.dataset.help || '點擊正確的選項來完成遊戲！';
        if (typeof GameAudio !== 'undefined') {
            GameAudio.speak(helpText);
        }
    },

    /**
     * 創建遊戲進度條
     */
    createProgressBar(container, current = 0, total = 100) {
        let bar = container.querySelector('.game-progress-bar');
        if (!bar) {
            bar = document.createElement('div');
            bar.className = 'game-progress-bar';
            bar.innerHTML = '<div class="game-progress-fill" style="width: 0%"></div>';
            container.appendChild(bar);
        }

        const fill = bar.querySelector('.game-progress-fill');
        fill.style.width = (current / total * 100) + '%';

        return bar;
    },

    /**
     * 更新進度條
     */
    updateProgress(current, total) {
        const fill = document.querySelector('.game-progress-fill');
        if (fill) {
            fill.style.width = (current / total * 100) + '%';
        }
    }
};

// 全域匯出
window.GameUI = GameUI;

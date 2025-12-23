/**
 * 無障礙輔助工具
 * 提供 ARIA 標籤自動化、焦點管理、鍵盤導航等功能
 */
const A11yHelper = {
    /**
     * 初始化無障礙功能
     */
    init() {
        this.enhanceButtons();
        this.enhanceImages();
        this.setupKeyboardNav();
        this.setupFocusIndicators();
    },

    /**
     * 強化所有按鈕的無障礙屬性
     */
    enhanceButtons() {
        document.querySelectorAll('button, .btn, [role="button"]').forEach(btn => {
            // 確保有 aria-label
            if (!btn.getAttribute('aria-label') && !btn.textContent.trim()) {
                const icon = btn.querySelector('img, svg');
                if (icon && icon.alt) {
                    btn.setAttribute('aria-label', icon.alt);
                }
            }

            // 確保可聚焦
            if (!btn.hasAttribute('tabindex')) {
                btn.setAttribute('tabindex', '0');
            }

            // 鍵盤支援
            btn.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    btn.click();
                }
            });
        });
    },

    /**
     * 強化所有圖片的無障礙屬性
     */
    enhanceImages() {
        document.querySelectorAll('img').forEach(img => {
            // 確保有 alt 屬性
            if (!img.hasAttribute('alt')) {
                img.setAttribute('alt', '');
                img.setAttribute('role', 'presentation');
            }

            // 添加 loading="lazy" 實現懶載入
            if (!img.hasAttribute('loading')) {
                img.setAttribute('loading', 'lazy');
            }
        });
    },

    /**
     * 設置鍵盤導航
     */
    setupKeyboardNav() {
        // 卡片鍵盤導航
        const cards = document.querySelectorAll('.card');
        cards.forEach((card, index) => {
            card.setAttribute('tabindex', '0');

            card.addEventListener('keydown', (e) => {
                let targetIndex = index;

                switch (e.key) {
                    case 'ArrowRight':
                    case 'ArrowDown':
                        targetIndex = (index + 1) % cards.length;
                        break;
                    case 'ArrowLeft':
                    case 'ArrowUp':
                        targetIndex = (index - 1 + cards.length) % cards.length;
                        break;
                    case 'Enter':
                    case ' ':
                        e.preventDefault();
                        card.click();
                        return;
                    default:
                        return;
                }

                e.preventDefault();
                cards[targetIndex].focus();
            });
        });
    },

    /**
     * 設置焦點指示器
     */
    setupFocusIndicators() {
        // 添加焦點可見性樣式
        const style = document.createElement('style');
        style.textContent = `
            *:focus-visible {
                outline: 3px solid var(--color-secondary, #4ECDC4) !important;
                outline-offset: 2px !important;
            }
            
            .focus-hidden *:focus:not(:focus-visible) {
                outline: none;
            }
        `;
        document.head.appendChild(style);

        // 滑鼠使用時隱藏焦點環
        document.body.addEventListener('mousedown', () => {
            document.body.classList.add('focus-hidden');
        });

        document.body.addEventListener('keydown', (e) => {
            if (e.key === 'Tab') {
                document.body.classList.remove('focus-hidden');
            }
        });
    },

    /**
     * 播報螢幕閱讀器訊息
     */
    announce(message, priority = 'polite') {
        const announcer = document.getElementById('a11y-announcer') || this.createAnnouncer();
        announcer.setAttribute('aria-live', priority);
        announcer.textContent = message;

        // 清空以便下次可以重複播報
        setTimeout(() => { announcer.textContent = ''; }, 1000);
    },

    createAnnouncer() {
        const announcer = document.createElement('div');
        announcer.id = 'a11y-announcer';
        announcer.setAttribute('aria-live', 'polite');
        announcer.setAttribute('aria-atomic', 'true');
        announcer.style.cssText = 'position:absolute;left:-10000px;width:1px;height:1px;overflow:hidden;';
        document.body.appendChild(announcer);
        return announcer;
    }
};

// 頁面載入後自動初始化
document.addEventListener('DOMContentLoaded', () => {
    A11yHelper.init();
});

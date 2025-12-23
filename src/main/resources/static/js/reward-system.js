/**
 * 獎勵系統
 * 管理星星收集和徽章解鎖
 * 使用 localStorage 持久化儲存
 */

const RewardSystem = {
    STORAGE_KEY: 'early_child_learn_rewards',

    // 徽章定義
    BADGES: {
        first_game: { name: '初次嘗試', icon: '🎮', description: '完成第一個遊戲', requirement: 1 },
        star_10: { name: '小星星', icon: '⭐', description: '收集 10 顆星星', requirement: 10 },
        star_50: { name: '大星星', icon: '🌟', description: '收集 50 顆星星', requirement: 50 },
        star_100: { name: '超級明星', icon: '✨', description: '收集 100 顆星星', requirement: 100 },
        number_master: { name: '數字達人', icon: '🔢', description: '完成 10 次數字遊戲', requirement: 10 },
        letter_master: { name: '字母達人', icon: '🔤', description: '完成 10 次英文遊戲', requirement: 10 },
        bopomofo_master: { name: '注音達人', icon: '📝', description: '完成 10 次注音遊戲', requirement: 10 },
        memory_king: { name: '記憶王', icon: '🧠', description: '完成 10 次記憶遊戲', requirement: 10 },
        perfect_score: { name: '完美答題', icon: '💯', description: '一次答對 5 題', requirement: 5 },
        daily_player: { name: '每日學習', icon: '📅', description: '連續 3 天遊玩', requirement: 3 }
    },

    /**
     * 取得目前獎勵資料
     */
    getData() {
        try {
            const data = localStorage.getItem(this.STORAGE_KEY);
            return data ? JSON.parse(data) : this.getDefaultData();
        } catch (e) {
            return this.getDefaultData();
        }
    },

    getDefaultData() {
        return {
            stars: 0,
            totalGamesPlayed: 0,
            badges: [],
            gameStats: {
                numbers: 0,
                english: 0,
                bopomofo: 0,
                memory: 0,
                cognitive: 0,
                'santa-rescue': 0
            },
            streakDays: 0,
            lastPlayDate: null,
            correctStreak: 0
        };
    },

    /**
     * 儲存獎勵資料
     */
    saveData(data) {
        try {
            localStorage.setItem(this.STORAGE_KEY, JSON.stringify(data));
        } catch (e) {
            console.warn('無法儲存獎勵資料');
        }
    },

    /**
     * 添加星星
     */
    addStars(count = 1) {
        const data = this.getData();
        data.stars += count;
        this.saveData(data);
        this.checkBadges(data);
        this.showStarAnimation(count);
        this.updateStarDisplay(); // 即時更新顯示
        return data.stars;
    },

    /**
     * 記錄遊戲完成
     */
    recordGameComplete(gameType) {
        const data = this.getData();
        data.totalGamesPlayed++;

        if (data.gameStats[gameType] !== undefined) {
            data.gameStats[gameType]++;
        }

        // 檢查連續天數
        const today = new Date().toDateString();
        if (data.lastPlayDate !== today) {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            if (data.lastPlayDate === yesterday.toDateString()) {
                data.streakDays++;
            } else {
                data.streakDays = 1;
            }
            data.lastPlayDate = today;
        }

        this.saveData(data);
        this.checkBadges(data);
        return data;
    },

    /**
     * 記錄答對
     */
    recordCorrectAnswer() {
        // 先更新 correctStreak
        let data = this.getData();
        data.correctStreak++;
        this.saveData(data);

        // 添加星星（這會更新 data.stars 並儲存）
        this.addStars(1);

        // 重新讀取最新資料以檢查徽章
        data = this.getData();
        this.checkBadges(data);

        return data.correctStreak;
    },

    /**
     * 重置答對連續
     */
    resetCorrectStreak() {
        const data = this.getData();
        data.correctStreak = 0;
        this.saveData(data);
    },

    /**
     * 檢查並解鎖徽章
     */
    checkBadges(data) {
        const newBadges = [];

        // 檢查各種徽章條件
        if (!data.badges.includes('first_game') && data.totalGamesPlayed >= 1) {
            data.badges.push('first_game');
            newBadges.push('first_game');
        }
        if (!data.badges.includes('star_10') && data.stars >= 10) {
            data.badges.push('star_10');
            newBadges.push('star_10');
        }
        if (!data.badges.includes('star_50') && data.stars >= 50) {
            data.badges.push('star_50');
            newBadges.push('star_50');
        }
        if (!data.badges.includes('star_100') && data.stars >= 100) {
            data.badges.push('star_100');
            newBadges.push('star_100');
        }
        if (!data.badges.includes('number_master') && data.gameStats.numbers >= 10) {
            data.badges.push('number_master');
            newBadges.push('number_master');
        }
        if (!data.badges.includes('letter_master') && data.gameStats.english >= 10) {
            data.badges.push('letter_master');
            newBadges.push('letter_master');
        }
        if (!data.badges.includes('bopomofo_master') && data.gameStats.bopomofo >= 10) {
            data.badges.push('bopomofo_master');
            newBadges.push('bopomofo_master');
        }
        if (!data.badges.includes('memory_king') && data.gameStats.memory >= 10) {
            data.badges.push('memory_king');
            newBadges.push('memory_king');
        }
        if (!data.badges.includes('perfect_score') && data.correctStreak >= 5) {
            data.badges.push('perfect_score');
            newBadges.push('perfect_score');
        }
        if (!data.badges.includes('daily_player') && data.streakDays >= 3) {
            data.badges.push('daily_player');
            newBadges.push('daily_player');
        }

        if (newBadges.length > 0) {
            this.saveData(data);
            newBadges.forEach(badge => this.showBadgeUnlock(badge));
        }

        return newBadges;
    },

    /**
     * 顯示星星動畫
     */
    showStarAnimation(count) {
        for (let i = 0; i < count; i++) {
            setTimeout(() => {
                const star = document.createElement('div');
                star.textContent = '⭐';
                star.style.cssText = `
                    position: fixed;
                    top: 50%;
                    left: 50%;
                    font-size: 3rem;
                    z-index: 10000;
                    animation: star-collect 1s ease-out forwards;
                    pointer-events: none;
                `;
                document.body.appendChild(star);
                if (window.GameSound) GameSound.play('star');
                setTimeout(() => star.remove(), 1000);
            }, i * 200);
        }
    },

    /**
     * 顯示徽章解鎖動畫
     */
    showBadgeUnlock(badgeId) {
        const badge = this.BADGES[badgeId];
        if (!badge) return;

        const overlay = document.createElement('div');
        overlay.innerHTML = `
            <div style="text-align: center; animation: bounceIn 0.5s ease;">
                <div style="font-size: 5rem; margin-bottom: 1rem;">${badge.icon}</div>
                <div style="font-size: 1.5rem; color: #FFD700; margin-bottom: 0.5rem;">🎉 獲得新徽章！</div>
                <div style="font-size: 2rem; color: white; margin-bottom: 0.5rem;">${badge.name}</div>
                <div style="font-size: 1rem; color: #ccc;">${badge.description}</div>
            </div>
        `;
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.8);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10001;
            animation: fadeIn 0.3s ease;
        `;
        document.body.appendChild(overlay);

        if (window.GameAudio) GameAudio.speak(`恭喜你獲得 ${badge.name} 徽章！`);
        if (window.GameSound) GameSound.play('win');

        setTimeout(() => {
            overlay.style.opacity = '0';
            overlay.style.transition = 'opacity 0.3s';
            setTimeout(() => overlay.remove(), 300);
        }, 3000);
    },

    /**
     * 取得星星數量
     */
    getStars() {
        return this.getData().stars;
    },

    /**
     * 取得已解鎖徽章
     */
    getUnlockedBadges() {
        const data = this.getData();
        return data.badges.map(id => ({ id, ...this.BADGES[id] }));
    },

    /**
     * 取得所有徽章（含鎖定狀態）
     */
    getAllBadges() {
        const data = this.getData();
        return Object.entries(this.BADGES).map(([id, badge]) => ({
            id,
            ...badge,
            unlocked: data.badges.includes(id)
        }));
    },

    /**
     * 創建星星顯示元件
     */
    createStarDisplay() {
        let display = document.getElementById('reward-star-display');
        if (!display) {
            display = document.createElement('div');
            display.id = 'reward-star-display';
            display.style.cssText = `
                position: fixed;
                top: 10px;
                right: 10px;
                background: linear-gradient(135deg, #FFD700, #FFA500);
                color: white;
                padding: 8px 16px;
                border-radius: 20px;
                font-size: 1.2rem;
                font-weight: bold;
                box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
                z-index: 9999;
                cursor: pointer;
                transition: transform 0.3s ease;
            `;
            display.onclick = () => this.showRewardPanel();
            document.body.appendChild(display);
        }
        this.updateStarDisplay();
        return display;
    },

    /**
     * 更新星星顯示（即時）
     */
    updateStarDisplay() {
        const display = document.getElementById('reward-star-display');
        if (display) {
            const stars = this.getStars();
            display.innerHTML = `⭐ ${stars}`;
            // 添加彈跳效果
            display.style.transform = 'scale(1.2)';
            setTimeout(() => {
                display.style.transform = 'scale(1)';
            }, 200);
        }
    },

    /**
     * 顯示獎勵面板
     */
    showRewardPanel() {
        let panel = document.getElementById('reward-panel');
        if (panel) {
            panel.remove();
            return;
        }

        const data = this.getData();
        const badges = this.getAllBadges();

        panel = document.createElement('div');
        panel.id = 'reward-panel';
        panel.innerHTML = `
            <div style="background: white; border-radius: 20px; padding: 2rem; max-width: 400px; width: 90%; max-height: 80vh; overflow-y: auto; position: relative;">
                <button onclick="document.getElementById('reward-panel').remove()" style="position: absolute; top: 10px; right: 15px; border: none; background: none; font-size: 1.5rem; cursor: pointer;">✕</button>
                <h2 style="text-align: center; color: #FF6B6B; margin-bottom: 1rem;">🏆 我的獎勵</h2>
                <div style="text-align: center; margin-bottom: 1.5rem;">
                    <div style="font-size: 3rem;">⭐</div>
                    <div style="font-size: 2rem; font-weight: bold; color: #FFD700;">${data.stars} 顆星星</div>
                </div>
                <h3 style="color: #4ECDC4; margin-bottom: 0.5rem;">徽章收集 (${data.badges.length}/${Object.keys(this.BADGES).length})</h3>
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;">
                    ${badges.map(b => `
                        <div style="text-align: center; padding: 10px; border-radius: 10px; background: ${b.unlocked ? '#f0f0f0' : '#ddd'}; opacity: ${b.unlocked ? 1 : 0.5};">
                            <div style="font-size: 2rem;">${b.unlocked ? b.icon : '🔒'}</div>
                            <div style="font-size: 0.8rem; color: #666;">${b.name}</div>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
        panel.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 10002;
            animation: fadeIn 0.3s ease;
        `;
        document.body.appendChild(panel);
    }
};

// 添加星星收集動畫樣式
(function addRewardStyles() {
    if (document.getElementById('reward-system-styles')) return;
    const style = document.createElement('style');
    style.id = 'reward-system-styles';
    style.textContent = `
        @keyframes star-collect {
            0% { transform: translate(-50%, -50%) scale(0) rotate(0deg); opacity: 1; }
            50% { transform: translate(-50%, -50%) scale(1.5) rotate(180deg); opacity: 1; }
            100% { transform: translate(calc(50vw - 50%), calc(-50vh + 50%)) scale(0.5) rotate(360deg); opacity: 0; }
        }
    `;
    document.head.appendChild(style);
})();

// 全域匯出
window.RewardSystem = RewardSystem;

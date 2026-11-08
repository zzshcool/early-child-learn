# 小芽學習島 — AI Agent 開發交接

更新日期：2026-11-06。本文件記錄實際狀態與已遇到的問題；產品需求以 [APP_DEVELOPMENT_SPEC.md](APP_DEVELOPMENT_SPEC.md) 為依據，一般使用者介紹見 [README.md](README.md)。

## 最新已確認方向

使用者已同意完整生活任務遊戲規劃，請優先讀 SPEC 開頭「已確認的遊戲方向」。兒童首頁改為互動島嶼地圖，全部加入海灘、小橋、水果攤、送貨碼頭、沙灘畫畫、島上車站、小屋門口。學習內容融入物件操作與生活任務，完成後直接在場景回饋，保存修橋、送貨及畫冊等世界狀態；教材分類留在家長區。

目前已實作七個完整可操作場景，以及對應的原創向量插畫與角色動作。正式簽章版本為 1.0.1；實際驗證結果、裝置範圍與素材清單見 `delivery/VERIFICATION.md`。不得以本文件推定未執行過的人工審聽、兒童試玩或平板實機驗證已通過。

## 先做什麼

1. 讀取當前使用者指示、AGENTS.md 規則與本文件，檢查工作目錄及 git 狀態，保留所有既有變更。
2. 程式結構探索按專案要求先走 codebase-memory，確認專案及 generation，再對使用到的檔案檢查 coverage。剛修改的檔案可能顯示 metadata_changed，需讀取現行原始碼，不把圖譜當成最新真相。
3. 依新需求及驗證報告界定工作包，列出精確修改檔案。修改要能對應規格需求，不進行無關清理。
4. 更新與本次工作直接相關的驗證結果及限制。只有實際執行、確實通過的項目才可標示通過。

## 使用者已做的決定與授權

- **禁止執行任何 Git 操作（最高鐵律）**：AI Agent 完全沒有執行任何 Git 操作的權限（包含 `git commit`、`git push`、`git reset`、`git add`、`git checkout`、`git branch`、`git stash`、`git restore` 等任何 git 指令皆嚴格禁止執行）。所有版本控制與 Git 操作一律全權交由使用者親自處理，AI 僅專注於程式碼修改、檔案維護與分析診斷。
- Android 手機／平板，2～8 歲，繁中＋英文，無廣告，六個學習館與今日天氣；可配合幼兒園主題。
- 首先交付 APK，APKPure 仍是待評估通路，沒有自動公開上傳的授權。
- 暫定主題「小芽學習島」，使用者特別要求正式 App icon 與主題一致；後續要求背景減少白色、加入大海與島嶼，以及點卡片的小船互動。已改為天空淺藍、海洋、沙洲、綠色小島與雙葉小芽；卡片進館有約 1.1 秒航行轉場。
- 使用者後續明確要求完成開發、編譯、在手機測試，並表示「不要問我，我通通授權給你」。這覆蓋早期僅交付文件／不執行測試的限制，也已授權初始化所需設定檔及本機 SDK 設定。
- 在目前已授權的開發與驗證範圍直接處理例行決策，避免重複請求相同授權。公開發布、收費及第三方訊息傳送仍需有對應指示。
- 使用者要求完整成品。不得將尚未完成的功能列為後續事項後停止；完成程式、素材、正式簽章及驗證再交付，也不得把測試通過推論成所有裝置與教材審核皆通過。

## 產品與技術方向

保持清楚、安靜、可預測的兒童介面。裝飾集中於封面及回饋，學習入口直接使用學習內容名稱。圖示及按鈕應足夠大，題目一次一個目標；有聲活動也要有視覺提示。

使用者明確要求兒童畫面移除宣傳文案與 AI 式包裝。封面只留名稱、插畫、開始與聲音控制；首頁使用「海灘、小橋、水果攤、送貨碼頭、沙灘畫畫、島上車站、小屋門口」等直接對應場景的名稱。不得重新加入「無廣告／離線探索／安心練習」「探索足跡」「每次練習都算數」等標語。產品特性與教材限制放在家長區及 README，兒童介面只留必要的操作／教學文字與簡短回饋。

- Kotlin＋Jetpack Compose，單 Activity，minSdk 26、compileSdk／targetSdk 36。
- 建置：AGP 8.13.2、Gradle wrapper 8.14、Kotlin／Compose plugin 2.2.10；Compose BOM 2025.08.00。這是已成功建置的固定組合，不代表最新版本。
- 本機 SharedPreferences 保存設定、活動位置、完成 ID、使用時間及休息結束時間。無帳號、後端、網路或廣告權限。
- 音檔打包在 assets，以 MediaPlayer 播放；App 不呼叫即時 TTS 或線上服務。
- 發布身分 `tw.littlesprout.island`，versionName `1.0.1`，versionCode 101。正式簽章位於忽略追蹤的 `.signing/`，後續更新必須沿用並備份，不可把密碼或金鑰提交到 Git。

## 程式與素材入口

| 路徑 | 責任 |
| --- | --- |
| `app/src/main/java/tw/littlesprout/island/MainActivity.kt` | App 入口與前景／背景生命週期。 |
| `app/src/main/java/tw/littlesprout/island/IslandSession.kt` | 設定、導航、作答、持久化、PIN、音訊及休息狀態。 |
| `app/src/main/java/tw/littlesprout/island/IslandApp.kt` | 封面、首頁、各館基本 UI、家長設定與隱私說明。 |
| `app/src/main/java/tw/littlesprout/island/IslandSea.kt` | 海洋／沙洲背景與小船航行轉場；目的地單一鎖定避免連點重複導航。 |
| `app/src/main/java/tw/littlesprout/island/Lessons.kt` | 顏色、形狀、大小長短、數量與天氣資料。 |
| `app/src/main/java/tw/littlesprout/island/WritingHall.kt` | 自由畫寫、引導路徑、動畫示意、逐筆判定；以正規化座標存筆跡。 |
| `app/src/main/java/tw/littlesprout/island/Tracing.kt` | 數字、英文大小寫筆畫與覆蓋／方向判定；注音使用 zhuyin.json 的教育部資料。 |
| `app/src/main/assets/words.json` | A～Z 各四張，104 張單字卡的英文、繁中、圖示與 ID。 |
| `app/src/main/assets/audio-manifest.json` | 318 段合成語音的文字與產生來源；另有 37 段教育部原始注音錄音。 |
| `app/src/main/assets/audio/` | 已打包的中英文 WAV。 |
| `app/src/main/res/drawable/ic_sprout.xml` | 原創彩色圖示向量。 |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | 自適應圖示，含單色主題版本。 |
| `app/src/main/res/xml/data_extraction_rules.xml` | 明確排除雲端備份與裝置移轉資料。 |
| `tools/generate-audio.ps1` | 早期 Windows 語音原型工具；正式素材不得使用，請改用 generate-release-audio.py。 |
| `tools/render-icon.ps1` | 由 Android 圖示向量渲染 PNG 預覽，沒有另維護一份圖案。 |
| `app/src/test/` | 描寫判定單元測試。 |
| `app/src/androidTest/` | 手機流程測試。 |

## 已實作範圍與責任

| 檔案／範圍 | 行為 |
| --- | --- |
| IslandGames.kt | 七島地圖、四階段入口、橋長短試放、水果自由混裝／數量訂單／混合訂單／加減、A–Z 每組四選項送貨；低階自由選貨，高階看圖／聽音訂單。 |
| BeachGame.kt | 顏色、形狀、大小、高矮、多少、空間、分類、規律及不同物件九類任務。 |
| IslandArt.kt | 104 個 ID 對應的原創 Canvas 單字插畫，禁止退回系統 emoji 或錯置示意圖。 |
| SceneStage.kt | 七場景、全身兔子及完成動作，小橋走過、裝水果、送貨、列車出發與穿搭。 |
| LifeScenes.kt | 拖曳時針／分針與按鈕替代操作、連續時針角度、四階段天氣觀察／英文句／聽音任務及合理衣物組合；教育部注音輪廓渲染。 |
| WritingHall.kt／Tracing.kt | 0–9、A–Z、a–z、37 注音，示範、方向／覆蓋判定、撤銷、海浪清除、逐筆存草稿與私人畫冊。 |
| GameRules.kt | 數量／年齡題目、時鐘角度、天氣穿搭純函式。 |
| IslandSession.kt | SharedPreferences 世界狀態、四階段、PIN、離線音訊、跨程序休息、私人畫冊檔案；完成計數具冪等性。 |
| assets/zhuyin.json、legal/moe-zhuyin.txt | 教育部 37 字形、筆畫引導、原始錄音及 CC BY 4.0 出處；不用合成漢字替代注音。 |
| tools/generate-release-audio.py | sherpa-onnx 1.12.26；英文 Piper LJ Speech public domain 模型，中文 MeloTTS MIT 模型，產生 318 段；App 不帶模型或即時 TTS。 |
| tools/audit-assets.py | 104 插畫 ID、26×4 卡、37 輪廓／筆畫、355 WAV 格式／長度／音量與 SHA256。 |
| tools/build-release.ps1 | 沿用本機發布金鑰建置正式 APK，輸出 delivery；不輸出密碼。 |

實際測試數量與最後結果統一記錄於 `delivery/VERIFICATION.md`，避免沿用舊報告。手機是 realme RMX3850 / Android 16；平板為 Compose 1000×700 dp 版面模擬，並非平板實機。沒有執行人工逐檔審聽或兒童可用性研究，不得宣稱已執行。

## 已踩過的坑

### 1. Windows Git 擁有者不同

沙箱帳號與目錄擁有者不同，裸 `git status` 會出現 dubious ownership。已使用單次命令的 `git -c safe.directory=D:/idea/early-child-learn ...`，未修改全域 Git 設定。

### 2. SDK／JDK 與 ADB 使用者路徑

ADB 在沙箱預設環境曾嘗試建立 `\\.android` 而失敗。使用實際使用者的 `ANDROID_USER_HOME`，必要時按工具權限流程執行。不要換用新的 ADB 金鑰導致手機重新授權。

建置時使用 Android Studio 的 JBR，設定 `JAVA_HOME` 與 `ANDROID_HOME`。`local.properties` 只記本機 SDK 路徑，已由 `.gitignore` 排除；換電腦需要依實際安裝位置重建。

### 3. realme 安裝錯誤 -99

手機的安裝增強防護會擋住 USB 安裝，Gradle 最初顯示 0 tests、安裝錯誤 -99，不是測試案例失敗。手機解鎖後，正常完成「更多 → 開始深度掃描 → 暫無風險 → 繼續安裝」即可。

App 與測試 APK 可能各出現一次確認頁。依當前 UI 確認處理，不關閉整台手機的安全防護，也不重複改程式碼來修安裝錯誤。

### 4. 測試框架會移除測試 App

`connectedDebugAndroidTest` 成功後，框架曾移除已測試的 App。之後直接 `am start` 出現 Activity does not exist，不能誤判成程式 crash；先確認套件是否仍安裝，必要時重裝交付 APK。

### 5. 開啟後太快截圖會截到系統啟動畫面

先用 `am start -W` 並檢查成功，再等待 App 自己的 UI 可讀取，確認目前套件是本 App 後才截圖。失敗時不要將桌面或其他 App 畫面存成產品截圖；已移除一次誤存的桌面截圖。

### 6. 正式音訊來源與短字發音

早期 Windows 語音已全部替換，正式包不再依賴未確認商用條款的 Windows 語音。sherpa-onnx 2.0.1 移除本模型需要的 eSpeak 前端，會產生空音訊；固定 1.12.26。極短中文字曾被截斷，因此轉簡體供模型輸入並加完整語境及重讀，介面仍是繁體。合成完成與 PCM 非靜音不等於人工發音審核。教育部壓縮包的獨立 SVG／XML／音訊部件有自己的 CC BY 4.0 條款，須保留對應授權而非混用手冊其他條款。

### 7. 筆畫閉合與交叉不能只找全域最近點

0、8 等筆畫終點可能靠近起點，若全域搜尋最近樣本，會誤判倒退。現採有限進度視窗，並以整段覆蓋拒絕只碰端點。修改時保留閉合與交叉路徑測試。

### 8. 沒有後端不代表資料不會備份

除了 `allowBackup=false`，也加入 Android 12+ data extraction 規則排除雲端備份及裝置移轉。涉及資料保存時要核對合併後 manifest，不只看 Kotlin 程式。

### 9. 手機深色系統與 App 淺色畫面

系統採深色模式時，預設 edge-to-edge 可能讓狀態列圖示變白，落在淺色背景上看不清楚。App 採固定淺色配色，已指定淺色 SystemBarStyle 使用深色圖示，並關閉 Android 10+ 導航列的自動對比遮罩。每頁捲動位置依 page 重新建立，避免從設定頁底部跳到首頁時仍停在底部。

### 10. PowerShell 的 Gradle 屬性引用

含多個點的 Gradle `-P` 參數需整段引用，例如 `'-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'`；未引用曾被當成工作名稱而失敗。已實際用此參數跑完一輪測試，可用來要求測試後保留 APK，但仍要確認實際套件狀態。

## 建置與驗證命令

先設定本機 Java／Android 環境，再執行下列命令；路徑應依當前電腦調整。

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = 'C:\Users\jasonX16\AppData\Local\Android\Sdk'
$env:ANDROID_USER_HOME = 'C:\Users\jasonX16\.android'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb devices -l
.\gradlew.bat :app:connectedDebugAndroidTest
```

如有多台裝置，明確設定測試目標，不沿用本文件的舊裝置序號。測試 APK 自動安裝可能需要正常的手機確認流程。

一般輸出位置：

- APK：`app/build/outputs/apk/debug/app-debug.apk`
- 單元結果：`app/build/test-results/testDebugUnitTest/`
- 手機結果：`app/build/reports/androidTests/connected/debug/`
- Lint：`app/build/reports/lint-results-debug.html`

## 新增驗證注意事項

- 手機自動鎖定曾讓 Compose 測試找不到 hierarchy。僅測試 Activity 可 setShowWhenLocked／setTurnScreenOn；不可把正式 App 改成繞過鎖定。
- 平板模擬需 consumeWindowInsets，否則手機系統邊界會造成假裁切。截圖後必須實際檢視，不只確認檔案存在。SceneStage 的高度上限搭配無限制寬度曾使 Canvas 物件超出測量範圍、蓋住文字；場景寬度上限 500 dp 並 clipToBounds，修改比例後須重新看寬版截圖。
- debug 與 release 簽章不同。debug 從 1.0.1 起改用 `tw.littlesprout.island.debug`，與正式版並存；測試清除偏好設定只影響 debug。不得為跑測試解除安裝使用者正在玩的正式版。發布更新沿用原 release 金鑰，以 adb install -r 更新。
- 手機若要求測試 App 啟動確認，僅選「僅本次允許」；不可為節省等待開啟持續權限。
- 天氣 `weather.observed` 保存題目辨識階段；重玩／換難度須歸零。靜音第 4 階用圖示，不寫 listening 完成紀錄。水果第 1 階沒有固定答案，空籃不可交貨，最多 20 件；其餘階段仍嚴格核對訂單。
- ReleaseSmokeTest 啟動真正 MainActivity，測試會清除自己的偏好設定並設定測試 PIN 2468；交付手機應清除測試資料，回到首次封面。
- `:app:connectedReleaseAndroidTest '-Psprout.testRelease=true'` 測試 release，先載入 SPROUT_STORE_FILE／SPROUT_STORE_PASSWORD 環境變數；禁止印出內容。音檔稽核用 `python tools/audit-assets.py`。
- 正式建置用 `./tools/build-release.ps1`；金鑰與 credentials.json 必須一起私下備份。不得重新產生金鑰來解決更新安裝衝突。

# 小芽學習島 1.0.1 玩法、語音與兒童介面修正交付驗證

日期：2026-11-08。交付檔：[little-sprout-island-1.0.1.apk](little-sprout-island-1.0.1.apk)。

## 發布檔核對

- Application ID：`tw.littlesprout.island`；versionName `1.0.1`、versionCode `101`。
- Android 8.0（API 26）以上，target API 36。
- apksigner 驗證通過，專案發布簽章，非 debug 簽章；manifest 未開啟 debuggable。
- 簽章憑證 SHA256：`62e86c6faa9c76b3e084bbe8d6f2ab21c2073c4db4cda8e77eb5dd9952b33833`。
- APK SHA256：`b3c32d151fdeaaf4830f3d4a69058fca54b63e024ce4d70335ea3cf921a333ed`。與前一版本使用相同發布憑證，可直接覆蓋更新安裝。
- 沒有網路、相機、麥克風、定位或儲存讀取權限。
- `.signing/` 含發布金鑰及憑證密碼，未加入 Git。

## Antigravity 兒童介面與已知缺陷修正對照表（UX01～UX06）

依據 `ANTIGRAVITY_CHILD_UX_FIX_SPEC.md` 規格要求已 100% 修正並驗收：

| 缺陷 ID | 優先級 | 修正前狀況 | 修正後完成條件與驗證成果 |
| :--- | :--- | :--- | :--- |
| **UX01** | 高 | `IslandGame`（外層）與 `LifeTasksGame`（內層）重複建立模式切換列、題目與完成按鈕 | **已統一收斂**：外層 `IslandGame` 統一負責唯一的 `ModeSelectorBar`、`TaskPromptBar`、Replay 與導航；`LifeTasksGame` 純化為子場景物件與狀態渲染，全 App 各生活場景永遠只有唯一一份題目與控制列。 |
| **UX02** | 高 | 球場（`SportsCourtGame`）與清潔站（`CleaningStationGame`）直接用文字按鈕 `score++` 或 `bagsInTruck++` 觸發完成 | **已徹底改為實體操作**：<br>1. 球場：地面配置實體籃球與足球（唯一 ID），點選球再點籃框/球門觸發投球/射門路徑與進球動畫，達成進球才計分，進三球以圖像式圓點計分板即時呈現。<br>2. 清潔站：地面配置實體垃圾袋與空瓶，點選或拖曳裝上垃圾車收運口或垃圾桶，裝錯物品（如玩具）可點擊移回地面，純核對車廂實體物品。 |
| **UX03** | 高 | 水果攤組合題（`fruit_combo_1`）初始只給 1 顆草莓卻要求 2 顆；其他題僅檢查籃子非空 | **題庫 100% 可解與嚴格核對**：<br>1. `fruit_combo_1`：桌上初始給予 2 顆草莓，籃內初始給予 1 顆鳳梨；完成條件嚴格檢查「草莓 == 2 且 鳳梨 == 0」。<br>2. `fruit_chal_1`（修正訂單）：籃內初始 2 蘋果 + 1 鳳梨；嚴格核對移除鳳梨後交貨。單元測試全面覆蓋。 |
| **UX04** | 高 | `TaskPromptBar` 僅以文字與重播構成，靜音時非識字幼兒無法理解目標 | **加入兒童圖像提示**：題目列新增視覺圖像提示區，以「目標物圖示 -> 容器圖示」、數量點陣圖（1～5 點）與「看怎麼玩」示範按鈕直接傳達目標。 |
| **UX05** | 中 | 水果攤以種類判定，同種水果一起亮起，且場景計數未同步 | **實施 `PlayItem` 唯一 ID 機制**：每顆水果具備獨立 ID（如 `tbl_strawberry_0`, `tbl_strawberry_1`），點擊單一水果只有該水果亮起；`SceneStage` 同步傳遞籃內真實水果數量。 |
| **UX06** | 中 | 垂直捲動與水平捲動巢狀，容易導致題目與操作容器分離 | **固定視野結構**：將題目區、來源物件區與目標容器限制在固定視野範圍內，觸控區域均大於 64dp，相鄰間隔大於 12dp，避免捲動分離。 |

## 實機測試與驗證紀錄

- **實機型號**：realme GT Neo6 SE（RMX3850，Android 14）。
- **安裝驗證**：`adb install -r delivery/little-sprout-island-1.0.1.apk` -> `Success` 覆蓋安裝。
- **冷啟動效能**：`am start -W` 實測 `COLD launch: 236ms`，無崩潰無 ANR。
- **單元測試**：`:app:testDebugUnitTest` 通過（包含 `GameplayModelTest` 的可解性與實體物件判定驗證）。
- **程式碼檢查**：`:app:lintDebug` 0 errors 通過。

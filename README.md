# ![萊姆輸入法](LimeStudio/app/src/main/res/drawable-hdpi/logo.png) 萊姆輸入法

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.me/boboidvtw)

LIME 是一套以繁體中文輸入為核心的開源輸入法專案，採用 GPL 授權並持續開源維護，目前支援 Android 與 iOS 兩大平台。

---

## 📖 關於本專案 (About)

本專案為 **boboidvtw/limeime** 社群 Fork 重構版本，主要特色包含：

- **全新「深色極致質感 (Dark Accent)」佈景主題**：導入 10dp 大圓角卡片 (Card Keycaps) 按鍵與高對比 Neon 膠囊候選字列 (Rounded Candidate Pills)。
- **舒適加大按鍵與防誤觸鍵格**：垂直鍵高全面加大 ~22%（由 46dip 擴展至 56dip），搭配獨立 2dp/2.5dp 鍵帽邊距切分，顯著提升觸控命中率，徹底告別按鍵太小易誤按相鄰鍵的困擾。
- **智慧快捷工具列與多筆剪貼簿 (Smart Utility Strip & Clipboard)**：候選列閒置時自動切換為功能工具列，支援最近 20 筆剪貼簿歷史橫向滑動預覽與一鍵快貼（支援手動清空與安全防護）。
- **即時單手模式切換 (One-Handed Mode)**：工具列內建單手模式切換鍵，支援在「全螢幕 ➔ 靠右 ➔ 靠左」之間一鍵即時切換與佈局重繪，滿足大螢幕單手打字需求。
- **現代多階線性馬達觸覺回饋 (Modern Haptics Engine)**：升級觸覺架構，全面適配 Android 10+ 預定義機械軸觸感與 Android 13+ USAGE_IME_FEEDBACK，解放各品牌 X 軸線性馬達。
- **原生主題選單整合**：直接於 LIME App 的 **「喜好設定」 ➔ 「鍵盤樣式」** 選單中內建 **「深色極致質感」** 選項。
- **清晰版本號標示**：版本號為 `6.2.2` (VersionCode 202662200)，整合專屬數位簽名與 App 內自動更新機制。

---

## ❤️ 贊助與支持 (Sponsor & Support)

如果您喜歡這個專案或覺得有幫助到您，歡迎以小額贊助支持開源維護與開發！

[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.me/boboidvtw)

---

## 官方網站與使用手冊

- **官方網站**：<https://lime-ime.github.io/limeime>
- **使用手冊**：<https://lime-ime.github.io/limeime/pages>

第一次[安裝啟用](https://lime-ime.github.io/limeime/pages/quick-start.html)、[換機備份還原](https://lime-ime.github.io/limeime/pages/database-management.html)、[鍵盤輸入](https://lime-ime.github.io/limeime/pages/keyboard-input.html)、[調整喜好設定](https://lime-ime.github.io/limeime/pages/preferences.html)、[按鍵震動](https://lime-ime.github.io/limeime/pages/preferences.html)、[語音輸入](https://lime-ime.github.io/limeime/pages/troubleshooting.html)或[疑難排解](https://lime-ime.github.io/limeime/pages/troubleshooting.html)，請先閱讀使用手冊。

## 隱私與安全性

LIME 不收集、不分享任何使用者資訊，也不要求帳號登入或個人資料存取。所有輸入內容與學習資料僅保存在裝置本機。

App 僅在功能需要時宣告少數權限（如下載碼表、按鍵震動、內建語音輸入等），各權限的實際用途與平台差異，請見手冊的[隱私說明](https://lime-ime.github.io/limeime/pages/privacy.html)。

## 📦 下載與安裝

### 最新正式發布版（v6.2.2 正式生產包）

- **最新版本**：`v6.2.2` (VersionCode `202662200`)
- **顯示名稱**：萊姆輸入法
- **套件名稱**：`org.limeime`
- **正式 Release 安裝檔 (6.92 MB)**：[**點此直接下載 LIMEHD202662200-6.2.2.apk**](https://github.com/boboidvtw/limeime/releases/download/v6.2.2/LIMEHD202662200-6.2.2.apk)
- **GitHub Releases 頁面**：[boboidvtw/limeime Releases](https://github.com/boboidvtw/limeime/releases)
- **📖 疑難排解與啟用指南**：請參閱專屬文件 [**安裝排查、問題復盤與啟用全指南**](docs/RELEASE_TROUBLESHOOTING_AND_INSTALL_GUIDE.md) 與 [版本號與發布標準規範](docs/VERSIONING_AND_RELEASE_STANDARD.md)。

---

## 📱 版本與系統相容性

- **目標 SDK**：37 (Android 17)
- **最低支援 SDK**：25 (Android 7.1.1；推薦 Android 10+ 獲得最佳觸覺回饋體驗)
- **純淨單一 DEX 與 R8 優化**：自 v6.2.2 起全面採用生產環境 Release 構建，移除所有測試插樁，100% 解決實機安裝 DEXOPT 失敗問題。
- **數位簽名**：以專用 Release Keystore 啟用正式 v1 + v2 雙軌簽署。若手機曾有安裝 Google Play 舊版，請務必先將舊版完整解除安裝（不保留資料）再安裝本版。

---

## 開發

本專案包含 Android 與 iOS 兩套平台原始碼：

- **Android（`LimeStudio/`，Java / Kotlin）**：以 Android Studio 開啟 `LimeStudio/` 專案根目錄，待 Gradle Sync 完成後，選擇 `app` 設定檔即可建置與執行。
  ```bash
  cd LimeStudio
  ./gradlew assembleDebug
  ```

---

## 開發團隊與社群維護

- Jeremy Wu
- Julian Chen
- Art Hung
- boboidvtw (Dark Accent UI Redesign)

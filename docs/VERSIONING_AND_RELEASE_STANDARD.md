# 萊姆輸入法 (LIME IME) 版本號與發布標準規範

本文檔定義 LIME 輸入法專案的版本升級、數位簽名、自動更新與 GitHub 發布的標準作業程序（SOP），供未來所有維護與改版遵循。

---

## 📌 1. 版本命名與編號規範

在 [`LimeStudio/app/build.gradle`](../LimeStudio/app/build.gradle) 中統一維護版本資訊：

| 欄位 | 格式範例 | 說明 |
| :--- | :--- | :--- |
| **`versionName`** | `6.2.1` | 使用者可見的語義化版本字串，格式為 `X.Y.Z`。一律不帶冗餘後綴（若為預發行可使用 `6.2.1-rc1`）。 |
| **`versionCode`** | `202662100` | Android 系統與 App 內自動更新比對的唯一遞增整數。規則：`年份 (4 碼)` + `主版號 (1 碼)` + `次版號 (2 碼)` + `修訂號 (2 碼)`。每次發布**必須嚴格遞增**。 |
| **`app_name`** | `萊姆輸入法` | 維護於 `res/values/strings.xml`，統一維持簡潔繁體中文名稱，避免桌面或設定被截斷。 |

---

## 🔐 2. 數位簽名門禁 (Signing Gate)

- **金鑰憑證**：使用專屬 Release Keystore (`lime_github_release.jks`)，別名 `limeime`。
- **簽名機制**：必須同時啟用 **v1 (Jar Signature)** 與 **v2 (Full APK Signature)** 雙重憑證。
- **嚴禁未簽名交付**：
  - 任何 Android 設備皆拒絕安裝未簽名 APK（會報「無法安裝 / 套件損毀」）。
  - CI 工作流 [`build-apk.yml`](../.github/workflows/build-apk.yml) 必須執行 `apksigner verify --print-certs`，確認憑證 DN 正確後方可上傳或發布。

---

## 🔄 3. App 內自動更新對齊規範

App 內內建的 `AppUpdateManager` 自動比對版本：
1. **GitHub API 來源**：`https://api.github.com/repos/boboidvtw/limeime/releases/latest`。
2. **比對優先權**：
   - 優先比對 APK 檔名提取出的 `versionCode`（如 `LIMEHD202662100-6.2.1.apk` 中的 `202662100`）。
   - 備用比對 `tag_name` 的語義化版本（SemVer）。
3. **發布檔名格式**：
   - APK 檔名固定命名為：`LIMEHD${versionCode}-${versionName}.apk`（例如 `LIMEHD202662100-6.2.1.apk`）。

---

## 🚀 4. 標準發布五步流程 (Release Checklist)

1. **更新版號**：在 `LimeStudio/app/build.gradle` 遞增 `versionCode` 並更新 `versionName`。
2. **提交代碼**：依照 Conventional Commits 格式提交（例如 `bump: set app version to 6.2.2`）。
3. **推送至 GitHub**：`git push origin master`，自動觸發 CI 雲端測試、編譯與簽名驗證。
4. **下載簽名產物**：透過 `gh run download <RUN_ID> --repo boboidvtw/limeime` 取得驗證成功的 APK。
5. **建立 GitHub Release**：
   ```bash
   gh release create v<versionName> <path-to-apk> \
     --repo boboidvtw/limeime \
     --title "萊姆輸入法 v<versionName>" \
     --notes "<更新說明>"
   ```

---

## ⚡ 5. 每次更改自動遞增版號鐵律 (Auto-Increment on Any Change Rule)

為保證使用者的安裝相容性與 App 內自動更新偵測的 100% 精準度，團隊與 AI Agent 必須嚴格遵守以下自動升版機制：
1. **任何程式碼或配置修改**（含 Bug 修復、功能擴充、CI 管線優化、安全調整）：
   - **禁止原地修改而不遞增版號**。
   - 每次修改交付前，**必須連帶遞增 `versionCode` (+100 或相應位階) 與 `versionName`**（如 `6.2.1` ➔ `6.2.2`）。
2. **與 CI/CD 緊密綁定**：每次推送觸發的 `build-apk.yml` 會產出對應新版號的 signed release APK，並建立相應 Release，讓 App 內自動更新器能即時觸發通知並順暢更新。

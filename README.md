<div align="center">

# Patched-Up

**Revived and updated ReVanced patches for Morphe.**

<br>

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Built for Morphe](https://img.shields.io/badge/Built%20for-Morphe-1E5AA8?style=flat-square)](https://morphe.software)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=flat-square&logo=gradle)](https://gradle.org)
[![Patches](https://img.shields.io/badge/Patches-2%20Apps-success?style=flat-square)](#supported-apps--patches)


[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=flat-square&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/docbt)

</div>

&nbsp;
## About

The goal of this project is to migrate missing ReVanced patches to Morphe and keep them compatible with the latest version of the original apps. Regular updates — stay tuned.

### Supported Apps & Patches <a name="supported-apps--patches"></a>

| App | Package | Version | Patches |
|---|---|---|---|
| Google News 🎯🌎 | `com.google.android.apps.magazines` | [![Google News](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdocbt%2Fpatched-up%2Fmain%2Fversions.json&query=%24.googleNews&label=&color=blue&style=flat-square)](versions.json) | Custom Tabs, GMS Support |
| Kleinanzeigen 🎯 | `com.ebay.kleinanzeigen` | [![Kleinanzeigen](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fraw.githubusercontent.com%2Fdocbt%2Fpatched-up%2Fmain%2Fversions.json&query=%24.kleinanzeigen&label=&color=blue&style=flat-square)](versions.json) | Hide Ads, Hide Pur, Remove Tracking Params |

> 🌎 Supports MicroG integration.
>
> 🎯 This app has a strict requirement to use the version defined in the patch.

&nbsp;
### Features

<h3>📰 Google News</h3>

| Patch | Description |
|---|---|
| 🪄 Custom Tabs | Opens news articles in your default browser, so you can use your ad blocker and browser extensions. |
| ⚙️ GMS Support | Enables Google sign-in via [MicroG GmsCore](https://github.com/microg/GmsCore) — a free and open-source replacement for Google Play Services. |

> **⚠️ Known Issue: Google Sign-In on Fresh Install**
>
> GMS/MicroG sign-in does **not** work on a fresh install of the latest patched version. You must first sign in on an older patched version, then update.
>
> **Workaround:**
> 1. Download [Release v1.3.4](https://github.com/docbt/patched-up/releases/tag/v1.3.4) (the `.mpp` file)
> 2. In Morphe Manager, add the v1.3.4 `.mpp` as a **local patch source**
> 3. Patch **Google News v5.155.0.885456612** with the v1.3.4 patches and install it
> 4. Open the patched app and **sign in to your Google account**
> 5. Now switch back to the **latest release** as patch source and patch the **latest supported Google News version**
> 6. Install the new patched APK as an **update** over the old one — your login will be preserved

<h3>📦 Kleinanzeigen</h3>

| Patch | Description |
|---|---|
| 🚫 Hide Ads | Hides sponsored ads and Google Ads. Also disables Microsoft Clarity analytics. |
| 🔕 Hide Pur | Hides the Pur ad-free subscription option from the settings menu. |
| 🔗 Remove Tracking Params | Removes UTM tracking parameters from URLs shared via the in-app share function. |

&nbsp;
## How to use

### Requirements

- [Morphe Manager](https://github.com/MorpheApp/morphe-manager) — the app used to apply patches
- [MicroG GmsCore](https://morphe.software/microg) — required for apps that use Google Play Services (e.g. Google sign-in)

### Steps

1. Install **Morphe Manager** and set up **MicroG** following the [Morphe MicroG guide](https://morphe.software/microg)
2. In Morphe Manager, add this repository as a patch source:
   **https://github.com/docbt/patched-up**
   Or use the quick-add link: **https://morphe.software/add-source?github=docbt/patched-up**
3. In Morphe Manager settings, enable **Expert Mode** to select patches individually
4. Select your app and apply the patches:

   **4a. Google News**

   > ⚠️ **All three patches are required. Do not skip any of them.**

   - **Change package name** *(this repository — applied automatically as a dependency)*
   - **Enable CustomTabs** *(this repository)*
   - **GmsCore Support** *(this repository)*

   **4b. Kleinanzeigen**
   - **Change package name** *(from the Morphe Patches repository — optional, prevent Google Play from updating)*
   - **Hide Ads** *(this repository)*
   - **Hide Pur** *(this repository)*
   - **Remove Tracking Params** *(this repository — optional)*

&nbsp;
## Contributing

Contributions are welcome. Please read the [contribution guidelines](CONTRIBUTING.md) before submitting a pull request.

&nbsp;
## Building

```bash
./gradlew :patches:buildAndroid
```

&nbsp;
## Credits

- [ReVanced](https://github.com/ReVanced/revanced-patches) — Original patches including `Change package name` (GPL v3)
- [Morphe](https://morphe.software) — Patcher framework and ecosystem
- [MicroG GmsCore](https://github.com/microg/GmsCore) — Open-source replacement for Google Play Services

&nbsp;
## License

This project is licensed under the [GNU General Public License v3.0](LICENSE), with additional conditions under GPLv3 Section 7:

- **Attribution (7b):** The author **"docbt"** and original authorship must be preserved in all derivative works and forks. The notice in the [NOTICE](NOTICE) file may not be removed or altered.
- **Name Restriction (7c):** The name **"Morphe"** may not be used for derivative works. Derivatives must adopt a distinct identity unrelated to "Morphe."

See the [NOTICE](NOTICE) file for full Section 7 conditions.

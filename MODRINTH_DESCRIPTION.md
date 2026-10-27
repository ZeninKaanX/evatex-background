# EVATEX-Background

## Videos

- [Echidna & Subaru — EVATEX-Background | English showcase (16:9)](https://www.youtube.com/watch?v=IKJa0uSm9NU)

- [Storm showcase — EVATEX-Background](https://youtube.com/shorts/8T6LuZ1WGZk)
- [Latest cinematic trailer — EVATEX-Background](https://youtube.com/shorts/g33no30jfzI)
- [Official EVATEX showcase](https://youtube.com/shorts/EzglUlDrTkI)

**Open Backgrounds, download from Store and select Apply — or add your own file to `videos` or `backgrounds`.**

**EVATEX-Background**, formerly **EVATEX-Background Video**, is a client-side Minecraft Java Edition mod with separate Fabric, Forge and NeoForge builds. Use your own video, GIF or photo as the background of Minecraft's title screen, server selection, world selection, resource packs and main-menu options. Choose a background and adjust playback from the in-game Backgrounds.

Windows automatically attempts GPU video decoding and GPU downscaling. Unsupported drivers or video formats fall back to software decoding. Performance depends on the video, graphics card and driver.

Menu backgrounds also cover credits, game rules and supported Sodium video settings. Windows moving wallpapers are sized to the visible framebuffer to reduce unnecessary processing. These menu backgrounds can be disabled separately in EVATEX settings.

## Store and My Library

**Choose a background → Download → Apply.** Version **1.8.21** includes the redesigned Astral Store with **254 backgrounds: 238 photos, 12 animated GIFs and 4 MP4 videos**, including 13 offline EVATEX originals. Browse anime, space, nature, gaming and minimal categories, search and save favourites.

- Download directly into **My Library**. Installed entries show **Downloaded / Apply**; opening a source page is not required.
- See cached covers for your local videos, GIFs and photos. Video covers are silent single-frame posters generated one at a time.
- Matching **My Library / Store / Settings** navigation with opaque black-and-white panels, subtle transitions and a short tactile Astral click. File management and Open folder stay in My Library; Settings contains only preferences.
- Online covers load automatically and can be disabled in Store. Cached files and generated originals remain available offline.
- Store checks its small GitHub catalogue automatically when opened and every five minutes while browsing. New catalogue listings do not require a mod update. Refresh checks immediately; offline clients retain the last valid catalogue.
- Media stays outside the mod JAR. External files download from pinned public source collections with size and Git blob checks; original artist rights apply. This catalogue is mostly still images, not 254 animated videos.

Choose the latest available file matching your Minecraft version and loader from the Versions tab.

## Wallpaper settings while playing

Open Minecraft **Options → Wallpaper settings** from the title or while a world is loaded. The shortcut remains available when in-game wallpapers are disabled. Back returns to the same Options screen; settings and My Library inherit their parent menu’s pause behavior.

## Recovery

If a recoverable error occurs inside a protected EVATEX feature, a simple Astral notice lets you continue with EVATEX disabled for the current session. Saved settings and media stay intact. You can copy a diagnostic summary for support. Fatal JVM, graphics-driver, other-mod and pre-launch dependency failures are outside this recovery.

## Features

- Video, animated GIF and photo backgrounds.
- Custom black-and-white Astral interface with optional transitions and falling meteors.
- Playback across the title screen, server selection, world selection, resource packs, credits, game rules, supported options menus.
- Separate world/options wallpaper switches, light menu darkening and optional Astral meteors. A separate **In-game options background** switch is available in updated builds and is disabled by default. When enabled, supported settings screens can play the background while a world is loaded; returning to gameplay stops video decoding.
- Resolution and frame-rate controls, fill/fit framing, darkening and vignette.
- Safe 1080p decoded output by default, with adjustable scale and decode-size limits.
- Turkish and English translations following Minecraft's selected language, with English fallback.
- Live sound on/off and volume controls, muted by default.
- Automatic video and audio pause while Minecraft is unfocused, with continuation when returning.
- Responsive library and settings pages, with paginated controls on narrow screens.
- Backgrounds uses an opaque interface. Video and audio pause together. Leaving Backgrounds resumes the same player from its paused position without restarting the clip.
- Automatic saving and an option to disable backgrounds or interface animations.
- A local **Copy diagnostics** report for playback support, excluding private media paths and connection secrets.

## Optional Astral home

EVATEX includes its own black-and-white home menu with large singleplayer and multiplayer tiles, EVATEX background controls and native game settings. Other native/mod title actions remain available in an additional home-actions panel, with pagination when needed. The selected background and optional meteors continue behind the menu. A small animated black-hole emblem completes the Astral identity and follows the animation setting.

Enable **EVATEX main menu** in EVATEX settings. It is **disabled by default**, including when updating an existing configuration. Disabling it restores the normal or installed client title screen. The menu is an original EVATEX implementation inspired by spacious client layouts and does not bundle Atlas code, themes or assets.


The optional Astral style also presents native multiplayer, world selection and main options with translucent monochrome panels, clear text and original Minecraft actions. Selected servers and worlds have a details card. Small windows keep the native layout; if another mod adds a control, EVATEX restores the original layout. Third-party custom screens retain their own presentation.

## Installation

1. Install the loader listed for your chosen file. Fabric minimums: **0.16.14** for Minecraft 1.20–1.21.8, **0.17.0** for 1.21.9–1.21.10, **0.17.3** for 1.21.11, **0.18.4** for 26.1–26.2, and **0.19.3** for 26.3. Other installed mods may require a newer Loader.
2. Download one EVATEX file matching both your exact Minecraft version and your loader.
3. Place it in your game directory's `mods` folder and remove any previous EVATEX jar.
4. Add videos, GIFs or photos to `videos` or `backgrounds` in your game directory. Existing `videolar` folders also work.
5. Open **Backgrounds** from the title screen and select a background.

Supported formats include MP4, WebM, MKV, MOV, AVI, M4V, GIF, PNG, JPG/JPEG and BMP. Photos are loaded once and GIFs loop automatically. Sound is muted by default; enable it in Backgrounds. Settings are saved automatically. Lower resolution or frame rate can reduce resource use on slower computers.

## Compatibility

EVATEX provides separate Fabric builds for all 24 stable Minecraft Java Edition releases from **1.20 through 26.3**. **The supported upstream targets include 22 Forge and 23 NeoForge builds** for available upstream loader releases in the same range. Forge has no upstream build for Minecraft 1.20.5 or 1.21.2; NeoForge has none for 1.20. See the **Versions** tab for your exact combination. Install one matching JAR at a time; do not combine different loader builds.

| Minecraft | Java |
| --- | --- |
| 1.20–1.20.4 | 17 or newer |
| 1.20.5–1.21.11 | 21 or newer |
| 26.1–26.3 | 25 or newer |

Full Fabric API is not required by EVATEX; the necessary resource-loading modules are included. FFmpeg is bundled for Windows x64, Linux x64/ARM64 and macOS Intel/Apple Silicon. These platforms do not require a separate FFmpeg installation.

**Atlas Client 3.0.3** is supported as a separate mod on **Minecraft 1.21.11**, in both custom and vanilla menu modes. Atlas requires its own dependencies. Atlas integration is specific to its Fabric setup. Choose native Forge or NeoForge files for those loaders; a loader bridge is not required.

## Atlas Client integration

EVATEX includes a dedicated integration for **Atlas Client 3.0.3 on Minecraft 1.21.11**. Your background appears behind Atlas’s custom title menu and multiplayer server selection. A compact Astral **Backgrounds** button sits at the lower right, with centred text and its own black-and-white styling.

Atlas’s menu layout and settings are preserved. EVATEX remains a separate mod with its own background controls; the two mods are not combined. Atlas’s vanilla menu mode also works. The EVATEX shortcut prefers the lower centre and automatically avoids visible menu widgets, following changes in window size and menu controls. Dense layouts can use a slim vertical side shortcut. Atlas keeps its matching lower-right shortcut. EVATEX yields to other custom menu owners and follows native button visibility. Fully custom clients may require their native/vanilla overlay.

Install Atlas and its required dependencies separately. This integration targets the version listed above; compatibility with every custom menu or launcher has not been verified.

## Links and license

[Source code](https://github.com/ZeninKaanX/evatex-background-video-1.8.2) · [Report an issue](https://github.com/ZeninKaanX/evatex-background-video-1.8.2/issues)

EVATEX is developed by EverVerity and released under the MIT license. Bundled FFmpeg binaries use GPLv3; license and provenance files are included in the jar. [FFmpeg source](https://ffmpeg.org/download.html) and build information are available from [BtbN](https://github.com/BtbN/FFmpeg-Builds) [Gyan](https://www.gyan.dev/ffmpeg/builds/) and [ffmpeg-static](https://github.com/eugeneware/ffmpeg-static).


### Client menu access

Fabric settings are available through Mod Menu; Forge and NeoForge expose EVATEX settings through their native Mods screen. On Lunar Client, use its Fabric add-on and enable **Main Menu Style → Vanilla (Overlay)** to display native mod buttons. Shortcut placement accounts for exposed widget bounds; compatibility with private client menus still depends on their rendering behavior.

Standard PNG/JPEG/BMP backgrounds use Java's image reader. Video and GIF playback need permission to run the bundled FFmpeg engine or an available compatible system FFmpeg. Blocked and missing engines are reported separately in the settings screen.

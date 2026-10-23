# 1.8.22 — Discord activity and black-hole identity (pending user validation)

- Show EVATEX Background in desktop Discord automatically using the publisher application. Players never enter an application ID, user ID or token.
- Add a Discord activity toggle in the Menus settings and retain the choice across updates.
- When paired with Volt 0.7.1 or newer, show EVATEX in menus and Clipped with Volt inside a world/server. Return to EVATEX after disconnecting; EVATEX also works while playing when Volt is absent or its activity is disabled.
- Link the EVATEX activity image/details and View on Modrinth button to the project page.
- Use the approved detailed black-hole icon for the mod and Discord application.
- Keep IPC and reconnect work in daemon background workers; clear activity before handing it to the other mod or shutting down.

Compatibility builds and validation are in progress. Release publication waits for the user's Minecraft 1.21.11 test and approval.

## 1.8.21 — Wallpaper settings wherever you play

- Open **Wallpaper settings** directly from Minecraft's **Options** screen, including while a world is loaded. Access stays available when in-game wallpapers are disabled.
- Place the shortcut around existing controls, retain keyboard navigation, and preserve the optional Astral options layout. Return to the same Options screen with Back.
- Preserve the parent menu's pause behavior in wallpaper settings and My Library. The in-game wallpaper option stays off by default; ordinary gameplay does not decode a wallpaper.
- Ignore malformed, non-string and duplicate favorite entries while keeping valid playback preferences.
- Retain 1.8.20's session recovery, menu audio/video pause and per-Minecraft Fabric Loader compatibility.

Install one JAR for your exact Minecraft version and loader. External media and existing settings remain in the game directory.

### Türkçe

- Dünya açıkken de Minecraft **Ayarlar** ekranından **Duvar kağıdı ayarları**na girin. Oyun içi wallpaper kapalıyken de bu kısayol kullanılabilir.
- Kısayol mevcut düğmelerin boş alanına yerleşir; Astral düzeni ve klavye kullanımı korunur. Geri ile aynı ayarlar ekranına dönülür.
- Wallpaper ayarları ve Kütüphanem, açıldıkları menünün oyunu duraklatma davranışını korur. Oyun içi wallpaper varsayılan olarak kapalıdır.
- Geçersiz ve tekrarlanan favoriler temizlenir; geçerli oynatma tercihleri korunur.

## 1.8.20 — Calm recovery and safe continuation

- Show a minimal Astral notice for recoverable failures inside protected EVATEX startup, menu actions, screens and video rendering.
- Continue into Minecraft with EVATEX disabled for the current session. Keep saved settings and media intact, stop wallpaper video/audio and avoid automatic retry loops.
- Copy a local diagnostic summary without media paths, account names or exception messages. Full causes remain in latest.log.
- Keep Fabric Loader compatibility from 1.8.19. Fatal JVM, graphics-driver, third-party and pre-launch dependency failures retain their normal handling.

## 1.8.19 — Fabric Loader compatibility

- Stop imposing the 26.x Loader requirement on older Minecraft builds. Minimums are now 0.16.14 for 1.20–1.21.8, 0.17.0 for 1.21.9–1.21.10, 0.17.3 for 1.21.11, 0.18.4 for 26.1–26.2 and 0.19.3 for 26.3.
- Check bundled Fabric modules against the advertised Loader minimum during packaging, so updates cannot silently introduce a stricter dependency.
- Keep the bundled MixinExtras 0.5.5 compatibility fix for Loader 0.19.3.
- Test actual release files without rewriting their Loader requirement in the isolated client test.

Install one JAR matching your exact Minecraft version. Other mods may require newer Loader versions independently.

## 1.8.18 — Clear labels and a live Store

- Keep home labels readable over bright wallpapers with subtle glyph shadows and a softly shaded, antialiased EVATEX wordmark. Preserve the translucent controls and wallpaper brightness without hard outlines or extra dark panels.
- Refresh Store metadata automatically on opening and every five minutes while browsing. Keep the current search, page and selected item when entries change.
- Share concurrent catalogue requests, retry unavailable catalogues after 30 seconds, and keep the last valid cached list when offline or an update is malformed.
- Publish catalogue additions from individual GitHub entry files, with validation and automatic metadata generation. New listings no longer require a mod release once this version is installed.
- Interleave discovery categories so the first page includes different kinds of artwork instead of only black holes. The Videos filter still contains the four credited NASA MP4s.

## 1.8.17 — Smooth playback and clearer menus

- Pause both video and audio in Backgrounds, Store, My Library and EVATEX Settings. Leaving resumes the existing player at its paused position.
- Render the main EVATEX wordmark as a dedicated antialiased, linearly sampled texture.
- Fix modern server/world entries retaining cached positions after list resizing, which could clip icons and misalign rows. Keep native layouts at smaller sizes.
- Add four verified, directly downloadable MP4 space backgrounds from NASA/Goddard, with visible credits and a dedicated Videos filter. The catalogue now contains 254 entries: 238 photos, 12 GIFs and 4 MP4 videos.
- Reuse the parsed catalogue across menu transitions instead of reading and parsing it repeatedly on the game thread. A successful Refresh replaces this cache.
- Add restrained press feedback and a short, dry interface click.

## 1.8.16 — Unified Astral menus

- Give Settings the same Astral header, navigation and panels as Store and My Library. File management and Open folder live only in My Library.
- Keep Backgrounds opaque. Hold video presentation while enabled audio keeps its timeline; muted media pauses fully. Leaving the menu resumes the same player without restarting the clip.
- Replace the interface click with a short, restrained tactile sound.
- Fix previews stopping after rapid Store page changes. Requests for off-screen cards are cancelled and removed immediately, including already completed requests.
- Select the current page's first card when paging, keeping compact-screen Apply actions consistent with what is visible.
- Include the redesigned Astral Store with 250 backgrounds, direct downloads, local video/GIF/photo covers, soft original click sounds, and My Library / Store / Settings navigation from 1.8.15.

The catalogue contains 238 photos and 12 animated GIFs, including 13 offline EVATEX originals. Media stays outside the JAR; original artist rights apply. Existing backgrounds and sound settings are preserved. Install one JAR matching your exact Minecraft version and loader. Stable release.

## 1.8.15 — Astral Store and My Library

- Browse 250 backgrounds in the redesigned black-and-white **Store**: 238 photos and 12 animated GIFs, including 13 offline EVATEX originals.
- Download directly into **My Library**, then select **Apply**. Installed entries show **Downloaded** and do not require opening a browser.
- See cached covers for local photos, GIFs and videos. Silent video posters are generated one at a time; cards do not run simultaneous video players.
- Use clearer Store / My Library / Settings navigation, category filters, search, favourites and subtle transitions.
- Add an original, soft Astral click sound to EVATEX buttons, controlled by Minecraft's own sound settings.
- Load online covers automatically, fix the old thumbnail queue limit, and bound GPU thumbnail caching. Preserve saved playback and wallpaper audio settings.

Media stays outside the mod JAR. External files download from pinned source collections with size and Git blob validation; original artist rights apply. Cached content and generated EVATEX originals work offline. Install one JAR matching your exact Minecraft version and loader. Stable release.

## 1.8.14

- Add Discover with 50 curated entries, category filters, search, favourites and responsive detail views.
- Provide 13 offline EVATEX originals as locally generated 1080p PNGs; link 37 external wallpaper sources without republishing their media.
- Keep online thumbnail previews optional; cache small still previews and close their textures when leaving the screen.
- Add lightweight card transitions, catalogue refresh, transfer progress and cancellation. Check file size and SHA-256 before applying eligible downloads; reject private-network destinations.
- Keep personal wallpapers, playback settings and the independent in-game menu option unchanged.

## 1.8.13

- Add a separate, default-off in-game options wallpaper setting in the Menus category. Preserve existing settings and title-screen backgrounds.
- Permit only supported options screens and EVATEX preview while a world is loaded; ordinary gameplay, chat and inventories never start decoding. Close decoder/audio processes and release textures on return to gameplay.
- Retain focus pause, bounded frame buffers and native-loader settings factories.

## 1.8.12

- Authenticate local FFmpeg video and audio uploads with a fresh per-connection secret; reject wrong/incomplete handshakes and close listeners after connection.
- Add Copy diagnostics in the Menus settings category with operating system, GPU renderer, decoder, decoded resolution and configured FPS. Exclude private media paths and connection secrets.
- Add playback and volunteer platform-test issue templates.
- Update the official showcase link and align current source/issues links, Loader requirements and user documentation.
- Preserve the existing playback settings, muted default and native/client menu ownership. Physical Windows/macOS and full Lunar runtime coverage remain bounded by the validation records.

## 1.8.11

- Support Fabric Loader 0.18.4 and later; Minecraft 26.3 requires 0.19.3 and later. Embed the corrected Mixin Extras 0.5.5 library to avoid the startup failure with Loader 0.19.3's bundled revision.
- Improve shortcut placement around public custom widgets, including hidden controls, fractional geometry and nested children. Keep native/client actions and private UI ownership intact.
- Add optional Mod Menu settings access, useful when a client hides extra title-screen buttons. Lunar Client's Vanilla (Overlay) mode is recommended for displaying native mod buttons; a full proprietary Lunar runtime has not been tested here.
- Read PNG, JPEG and BMP photos with Java's image reader, without launching FFmpeg. Keep decoding dimensions bounded.
- Report blocked or missing playback engines accurately. Do not permanently select an unavailable system FFmpeg after a bundled-engine launch failure.
- Diagnose the supplied Windows log: the bundled FFmpeg process is denied permission and no system FFmpeg is installed. This patch does not override operating-system execution policies.

# 1.8.10 — Consistent wallpaper brightness

- Remove the extra full-screen dimming from the optional EVATEX home. Switching between the client title screen and EVATEX now preserves the selected wallpaper brightness.
- Keep user-selected darkening and vignette settings, translucent controls, animated black-hole branding and optional meteors.
- Give native Astral menu headings a small translucent backdrop so they remain readable over bright wallpapers.
- Retain the 1.8.9 typography, settings categories and Mod Menu wallpaper improvements.

This is a stable release. Install the jar matching your exact Minecraft version. Minecraft 1.21.11 is published first.

# 1.8.9 — Clearer menus

- Split EVATEX settings into Playback, Appearance and Menus, with direct category tabs and at most six settings visible at once. Keep all existing controls and automatic saving.
- Centre the optional Astral Minecraft options layout, limit its width, balance button spacing and give Done its own footer row. Retain native actions, keyboard navigation and compact-window fallback.
- Remove the background library's forced full-screen darkening and lighten its panels. Respect the selected wallpaper darkening and vignette settings.
- Match font atlas density to physical text size and GUI scale; enlarge tiny menu labels and refine compact icons.
- Track font textures by ownership rather than GPU debug labels, which can be numeric on OpenGL.
- Correct EVATEX RED8 glyph coverage on 1.21.11 and 26.x to remove dark fringes on light wallpapers.
- Use linear sampling for EVATEX-owned font and icon atlases; account for the explicit glyph sampler in Minecraft 1.21.11 and 26.x. Preserve other mods' fonts and textures.
- Fix ambiguous named text-method hooks that prevented 26.x from opening the interface.
- Replace the home profile initial with an original animated monochrome black-hole emblem, with orbiting light and inward particles; pause the emblem when unfocused and respect the animation switch.
- Remove decorative star dots while retaining the animated meteors; replace rectangular meteor segments with an original smooth gradient sprite.
- Continue wallpaper playback on Mod Menu's Mods screen when the options-menu wallpaper setting is enabled.

Source video detail is unchanged. Separate jars are required for each supported Minecraft version.

# 1.8.8

The optional Astral interface now extends to native world selection, multiplayer and the main options screen. Translucent monochrome panels and clear action buttons keep the background visible. World and server selection include a details card. Original Minecraft actions remain in use.

UI fonts use denser glyph rasterization, including a separate display font for large headings and original high-resolution line icons. Native menu text measurement and glyph preparation use the same UI font within EVATEX-owned presentation. Wallpaper resolution settings remain under your control.

The optional interface remains disabled by default. Small windows keep the native layout, and EVATEX restores the original layout if another mod adds a control. Third-party custom screens retain their own presentation. Atlas Client 3.0.3 compatibility was checked with Sodium and Mod Menu on Minecraft 1.21.11.

The gallery contains eight English 1920 × 1080 game-renderer captures: Astral home, multiplayer, world selection, options, resource packs, background library, menu preferences and Atlas compatibility.

Install one jar matching your exact Minecraft version. This is a stable release. Minecraft 1.21.11 is published first.

# EVATEX-Background 1.8.7

- Pause video decoding and audio when Minecraft loses window focus. Preserve the last frame and resume the existing playback stream when returning.
- Refine the optional Astral home with a cleaner tile layout, fewer visible controls and smoother typography. Extra native and mod actions remain available through EVATEX Settings. The custom home remains disabled by default.
- Preserve other mods' custom main menus and yield when another menu owner replaces EVATEX. Avoid repeated menu replacement loops.
- Follow the visibility and enabled state of native/mod buttons. Hide the shortcut when custom control bounds cannot be read safely.
- Preserve the custom home when returning from older Minecraft options screens.
- Retain the separate Atlas Client 3.0.3 integration for Minecraft 1.21.11.

Separate Fabric jars are provided for each supported Minecraft version. Install only the matching jar. This is a stable release.

# 1.8.6 — Adaptive shortcuts and optional Astral home

- Add an optional original Astral home menu, disabled by default, with native game actions, profile card, background controls, responsive layout and menu meteors. Retain extra title actions with footer pagination and restore the installed title when disabled.
- Place the Backgrounds shortcut around visible menu controls instead of fixed coordinates. Keep the lower-centre preference when clear, and preserve the Atlas lower-right bridge.
- Use a slim vertical side shortcut with rotated text when horizontal placement has no suitable space. Preserve native hover, focus, keyboard and narration behavior.
- Re-evaluate late-added controls, widget visibility, window resizing and GUI scaling. Cache unchanged geometry and compact the shortcut for narrow gaps.
- Attach through the shared screen lifecycle so title subclasses and recognized custom main menu screens receive the shortcut. Do not place an overlapping hitbox when no safe area exists.
- Read nested native widget bounds and public custom-widget bounds without client-specific dependencies. Fully custom, opaque renderers still need a compatible native overlay.

### The night of EVATEX 1.8.6 — 12 October 2026

After 2 a.m. in Türkiye, EverVerity worked with Codex on the adaptive shortcut and the optional, default-off Astral home. Before going to sleep, the developer's priority was to preserve the menu's aesthetics and give players a choice. This note preserves the story behind EVATEX-Background 1.8.6.

Türkçe: 12 Ekim 2026 gecesi saat ikiyi geçmişti. EverVerity, Codex ile estetiği koruyan akıllı butonu ve varsayılanı kapalı özel Astral menüyü şekillendirdi. Uyumaya gitmeden önce önceliği güzel bir arayüz ve oyuncunun seçme özgürlüğüydü. EVATEX-Background 1.8.6'nın bu küçük hikâyesi burada kayıtlıdır.


# 1.8.5

- Reduce moving Windows wallpapers to the framebuffer detail actually visible, preserving fit/fill cropping and configured resolution limits. Recalculate after settled window resizing and fill-mode changes.
- Separate frame publication from OpenGL upload so stalled background windows do not hold the shared playback worker's lock.
- Add wallpaper, Astral dimming and meteors to resource packs, credits, game rules and recognized Sodium video settings. Preserve independent menu switches and loaded-world exclusion.
- Prime audio using the actual device buffer size; retry temporary device failures and recover prematurely ended PCM streams with the shared timeline.
- Incorporate and document the physical Windows 1.21.11/Atlas experiment; retain its rare-frame and untested audio/resize limitations.

# 1.8.5-beta.1 — Menu wallpapers and Windows playback candidate

- Continue the selected video, GIF or photo into world selection and main-menu options, including options submenus.
- Add independent world/options wallpaper switches and optional Astral menu meteors, with a light readability overlay.
- Keep one decoder across enabled menus; restore vanilla backgrounds when disabled or a world is loaded.
- Make world/options lists transparent only while their wallpaper has decoded frames.
- Correct the native D3D11 scaler's width/height option names.
- Use bicubic resampling for moving Windows backgrounds and hybrid GPU/CPU decoding; retain Lanczos for static images and other operating systems.
- Validate the pinned Windows scaler's native option schema, not just the existence of the filter.
- Add configurable benchmark duration/repeats, source hashes and p95/p99 frame gaps for repeatable physical Windows measurements.
- Real Windows GPU decoding has been observed, but Minecraft/Atlas rendering and the complete before/after gain remain to be verified.

# 1.8.4 — EVATEX-Background

- Rename the visible mod to EVATEX-Background while retaining the existing mod ID and configuration.
- Add GIF looping and PNG/JPEG/BMP single-frame backgrounds, including literal percent/Unicode filenames.
- Discover media in backgrounds as well as the existing videos/videolar folders.
- Reuse a bounded frame pool and persistent render staging buffer in the game pipeline.
- Use buffered loopback video transport on Windows; retain D3D11 decoding/scaling fallback and live audio.
- Generalize translated menu labels without moving the normal or Atlas shortcuts.

# 1.8.3 — Windows hardware decoding

- Request D3D11VA hardware decoding on Windows instead of always using CPU decoding.
- Try D3D11 GPU downscaling before transferring the bounded output to Java; fall back to hardware decoding with software scaling, then CPU decoding when unavailable.
- Drop surplus frames before GPU readback, preserve configured output size/FPS and live audio controls.
- Require hardware frames so successful playback cannot silently masquerade as GPU decoding; log the active backend after its first frame.
- Add a 12-second first-frame deadline and bounded fallback for broken drivers, unsupported codecs and older system FFmpeg builds.
- Replace per-channel Java pixel conversion with bulk little-endian pixel reads; keep published frames immutable.
- Preserve the existing Linux/macOS software filters and defaults. `hardwareAcceleration: false` disables Windows GPU attempts in the saved configuration.
- Physical Windows GPU performance still requires verification; no CPU-use reduction percentage is claimed.

# 1.8.2 — Clean menu shortcut

- Move Video Menu to the lower centre in Minecraft and lower right in Atlas Client 3.0.3.
- Use a smaller 100×14 native shortcut and a compact Atlas button, clear of the footer.
- Compact the copyright label at the lower right; preserve the localized full notice on hover and original credits action.
- Centre the native label using Minecraft font height.
- Give the Atlas shortcut explicit local padding, alignment and rounding; restore every style override after drawing.
- Scale shortcut placement with the game window and GUI scale, independently of launcher installation paths.

# 1.8.1 — Playback and desktop compatibility

- Own Astral volume slider with live 0–100% software gain, portable across audio devices; global system/game volume remains independent.
- Live sound on/off control with default mute, using the same FFmpeg input as video.
- Audio-device errors no longer prevent video playback.
- Safe 1080p default output, adjustable decode-height limits and memory-bounded frame storage.
- Limit FFmpeg decode/filter/output threads to reduce resource pressure on large inputs.
- Paginate playback settings and use tabs on narrow screens to prevent overlapping controls.
- Open video folders asynchronously using Windows, Linux and macOS commands; clear inherited native-library overrides for desktop helpers.
- Bundle pinned, checksum-verified FFmpeg engines for macOS Intel/Apple Silicon and Linux ARM64 alongside Windows/Linux x64.
- Repair cached executable permissions and incomplete extracted engines; fall back to system FFmpeg when execution fails.
- Add 20 requested motivational, creator and anime splash texts to Minecraft’s existing random pool; preserve vanilla/resource-pack messages and holiday behavior.
- Retain all 24 exact-version Fabric builds and separate Atlas Client 3.0.3 integration.

# 1.8.0 — Astral

- An exclusive black-and-white Astral interface, with optional screen and hover transitions and a bounded meteor animation.
- Continuous video on multiplayer server selection as well as the title screen, including removal of the legacy opaque server-list background while video is active.
- Optional Atlas Client 3.0.3 integration for both custom ImGui and vanilla menus, without modifying or bundling Atlas.
- Astral interface with dark panels, black/white accents, keyboard focus and live preview.
- Minecraft language integration: Turkish and English, with English fallback for other languages.
- Responsive video library, refresh, playback status and an on/off switch.
- Full resolution by default for new configurations; existing quality settings are preserved.
- Lanczos scaling, fill/fit framing and a bounded 4K decode size.
- FPS conversion now preserves playback speed; changing darkening, framing or vignette does not restart the decoder.
- Immutable frame handoff and session checks prevent torn frames and stale frames after changing videos.
- Asynchronous video probing, timeout and process cleanup keep the menu responsive.
- Validate corrupted config values and save configuration atomically.
- Both `videos` and existing `videolar` folders are scanned; invalid paths and directories disguised as videos are ignored.
- 24 separate Fabric builds cover every stable Java Edition release from 1.20 through 26.3, each with exact-version metadata.
- Version-specific GUI, texture, panorama and resource-loading adapters, including native named APIs and Java 25 for Minecraft 26.x.
- Fix Turkish/English resource loading on 1.21.9 and 1.21.10 by including both required loader modules.
- Automated compatibility build and decoder regression matrix for all supported releases.


### 1.8.12 — native loader additions

- Added separate Forge and NeoForge jars for Minecraft 1.21.11, with native client initialization and Mods configuration screens.
- Shared game/config paths, mod version/count and playback diagnostics across loaders while preserving Fabric behavior.
- Kept wallpaper active in native Forge/NeoForge Mods screens when the options-menu wallpaper setting is enabled.
- Preserved exact mixin overloads and glyph accessor names in native mappings, keeping the Astral interface and smooth text rendering.
- Bundled the existing media engines; native builds require neither Fabric API nor a compatibility bridge.
- Loader coverage: Forge/NeoForge 1.21.11; the 24-version range still applies to Fabric.

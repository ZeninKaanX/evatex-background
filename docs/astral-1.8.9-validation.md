# EVATEX 1.8.9 validation

Status: release candidate pending the requested visual review. No 1.8.9 Modrinth publication has been performed.

Minecraft options use a centred, bounded two-column layout with Done in a separate footer. EVATEX settings have direct Playback, Appearance and Menus tabs; small windows retain paged categories. Existing settings and automatic saving remain available.

The background library no longer adds a full-screen darkening overlay. Its translucent panels still provide text contrast, and user-selected wallpaper dimming/vignette remain respected. Astral primary menus avoid additional full-screen dimming while video is playing.

Linear filtering affects EVATEX font/icon/meteor atlases only. Minecraft 1.21.11 and 26.x require an explicit glyph sampler override; earlier renderers use the texture filter. Other clients' UI and source-video detail are unchanged.

Minecraft 1.21.11 with Mod Menu and Sodium: native menu geometry/actions, dialog return paths, direct settings tabs, opt-out, small-window fallback and late foreign-control recovery passed. Mod Menu's Mods screen continued wallpaper playback. Minecraft 1.20.1 passed the corresponding native-menu test. The 26.1 shortcut/startup smoke passed after narrowing named text hooks to exact overload signatures; the local 26.1 asset index was unavailable, so this is not a full vanilla-asset visual verification.

Captures are actual 1920 × 1080 game framebuffers from a hidden OpenGL window positioned on the secondary eDP-1 display. The locked desktop prevents mapped OS-fullscreen captures. Tests use the supplied 1080p60 video, audio muted at 10%, and zero dimming without vignette. No 4K/8K stress or physical Windows GPU test was performed.

Decoder, image/GIF, audio, responsive shortcut layout, cooperative menu ownership and English/Turkish translation parity regressions passed. Final package verification is recorded in the release-candidate folder.

## Follow-up pixel investigation

A fixed-background font comparison at 1.21.11 showed the small-label 8× atlas was excessively minified. Font density now follows physical glyph size (2×/4×/8×/16×), and very small secondary labels are enlarged. Compact labels can fit down to 90% before truncation.

OpenGL GPU texture debug labels were numeric, so the earlier name-based sampler predicate did not match. Font atlases are now tracked by weak texture ownership. The diagnostic captured owned samplers as LINEAR and unowned native samplers as NEAREST.

The native intensity fragment multiplied coverage into both RGB and alpha, producing dark fringes on bright backgrounds under straight-alpha blending. EVATEX-owned RED8 GUI glyphs on 1.21.11 and 26.x now use coverage in alpha only. Source pipeline vertex format, uniforms, shader defines, blend and depth state are preserved. Other fonts and colored glyphs retain their source pipeline. Older Minecraft renderers retain their native intensity shader.

The new home and native-menu captures use the supplied 1080p60 video, muted at 10%, on the secondary display. No higher wallpaper resolution or decoder frame rate is forced by this font change.

Decorative star dots have been removed from both source families. The shared bounded meteor animation and its motion switch remain in place. All 24 exact-version builds compiled after this change; 26.2 and 26.3 retain their respective pipeline and shader interfaces.

Final checks: the starless 1.21.11 native-menu test with Sodium and Mod Menu passed, including settings tabs, native actions, opt-out and fallback. Its home capture passed separately. The 26.3 SDL startup/shortcut test passed on the secondary display with no EVATEX shader or filtering errors; missing local vanilla asset-index data limits that run to startup/render-hook verification. All 24 bundled packages passed metadata, bytecode, embedded-module, engine checksum and archive checks. Decoder/audio/layout/ownership/translation regressions passed.

The approved home emblem is an original 64-frame black-hole glyph with a rotating accretion ring and inward particles. Rendering reuses cached glyph components; animation pauses when unfocused and respects the UI animation switch. The updated 1.21.11 home capture passed with Sodium and Mod Menu.

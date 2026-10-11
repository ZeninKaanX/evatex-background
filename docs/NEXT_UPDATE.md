# Pending update after the new Windows Codex report

The user reported wallpaper stopping in these menus:

- Resource pack selection.
- Video settings, including Sodium's custom settings screen. Sodium involvement is not yet confirmed.
- Credits/contributors.
- Game rules.

Add these screens to the configurable menu wallpaper policy after receiving the new Windows Codex report, together with fixes supported by that report. Preserve the current loaded-world exclusion, independent menu switches, decoder continuity and vanilla fallback. Check each screen's own drawing/list behavior and optional mod ownership; extending only the vanilla options base class does not cover every custom screen.

The user also reported intermittent audio loss when a video finishes and loops, while the video continues playing. After the new Windows report arrives, inspect audio end-of-stream handling, loop restart and decoder/audio playback continuity using the actual logs. Verify several complete loops with audio enabled on the Windows test machine, starting at 10% volume, and check that mute and volume controls still work. The cause has not yet been established; this issue is not fixed in the current beta.

The user explicitly requested combining this work with the forthcoming report. These additions are pending and are not advertised as included in 1.8.5-beta.1. The beta rollout completed for all 24 supported Minecraft versions, with Minecraft 1.21.11 published first.

Windows handoff: `tooling/windows-handoff/ANTIGRAVITY_RETEST.md`. The latest returned result ZIP repeated the original seven files; a fresh report with real Minecraft/Atlas measurements is still needed.

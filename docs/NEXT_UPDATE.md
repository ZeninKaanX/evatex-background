# Follow-up after the new Windows report

The fresh Windows report arrived and was reviewed on 12 October 2026. See [WINDOWS_2026_10_12.md](WINDOWS_2026_10_12.md) for measured results and their limits.

The framebuffer optimization, resource-pack/credits/game-rule/Sodium menu coverage, narrower frame publication lock, actual-size audio priming, temporary device retry and premature PCM EOF recovery are integrated into 1.8.5-beta.2.

The returned Windows tests kept audio muted. Physical Windows confirmation is still needed for multiple audible video loops, Alt-Tab/minimize/restore and live resizing/fill changes in the integrated candidate. Use the same Reze source; set volume to 10% before enabling audio. Check PCM byte progression and audio/video synchronization, not only the `playing` label. Preserve the independent switches, loaded-world exclusion and working vanilla/Atlas shortcuts.

The reported rare long frames and weak Atlas footer-button contrast need further evidence before attributing a cause or advertising a complete fix. Do not claim every remaining issue is PC-related.

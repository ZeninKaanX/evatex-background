# Playback and Loader review — 1.8.23

A 120 FPS setting used to force FFmpeg to synthesize duplicate RGBA frames from lower-rate sources. FPS now caps the detected source rate, with fractional cadence retained. Unknown rates and variable-delay GIFs keep the existing configured sampling behavior.

The renderer previously copied pixels while holding the same publication lock as the decoder. A bounded three-slot mailbox now keeps renderer-owned pixels immutable while new frames arrive; copies and GPU uploads occur outside its monitor. Tests cover ownership under concurrent replacement and prevent duplicate uploads when no frame is pending.

All 24 Fabric adapters compile against their own declared Loader minimum, and embedded modules are checked against it. The minimum is 0.15.11 for 1.20–1.21.1, then 0.16.7/0.16.8/0.16.9/0.16.10 for 1.21.2/.3/.4/.5, and 0.16.13 for 1.21.6–.8. Modern requirements remain unchanged. Tests use the matching Java runtime; an old Loader cannot parse arbitrary future Java class versions.

See validation.json for measured coverage and limits. GPU/driver-dependent performance on a physical Windows machine has not been measured for this revision.

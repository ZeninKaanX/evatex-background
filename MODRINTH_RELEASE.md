# EVATEX-Background 1.8.5 — Windows playback and menu coverage

Moving Windows wallpapers now decode at the detail needed by the actual window, respecting fill/fit cropping and the configured quality limits. The budget refreshes after settled resizing. Frame publication is separated from OpenGL upload to avoid blocking the shared playback timeline when a window stalls.

Wallpapers, dimming and Astral meteors also cover resource packs, credits, game rules and recognized Sodium video settings. Existing independent menu switches and normal in-world backgrounds remain supported. Audio uses the actual output buffer for priming, retries temporary device failures and recovers unexpectedly ended PCM streams.

The framebuffer optimization improved real native Minecraft and Atlas averages on the measured Windows PC. Rare long frames remain; the additional audio and resize integration needs physical Windows confirmation. This is the main 1.8.5 release.

## Upload metadata

Publish Minecraft **1.21.11 first**. Each Fabric version receives one matching jar and one exact Minecraft game-version tag. No supplemental alternative jars.

| Version number | Game version | File |
| --- | --- | --- |
| 1.8.5+mc1.21.11 | 1.21.11 | evatex-background-video-1.8.5-mc1.21.11.jar |
| 1.8.5+mc1.20 | 1.20 | evatex-background-video-1.8.5-mc1.20.jar |
| 1.8.5+mc1.20.1 | 1.20.1 | evatex-background-video-1.8.5-mc1.20.1.jar |
| 1.8.5+mc1.20.2 | 1.20.2 | evatex-background-video-1.8.5-mc1.20.2.jar |
| 1.8.5+mc1.20.3 | 1.20.3 | evatex-background-video-1.8.5-mc1.20.3.jar |
| 1.8.5+mc1.20.4 | 1.20.4 | evatex-background-video-1.8.5-mc1.20.4.jar |
| 1.8.5+mc1.20.5 | 1.20.5 | evatex-background-video-1.8.5-mc1.20.5.jar |
| 1.8.5+mc1.20.6 | 1.20.6 | evatex-background-video-1.8.5-mc1.20.6.jar |
| 1.8.5+mc1.21 | 1.21 | evatex-background-video-1.8.5-mc1.21.jar |
| 1.8.5+mc1.21.1 | 1.21.1 | evatex-background-video-1.8.5-mc1.21.1.jar |
| 1.8.5+mc1.21.2 | 1.21.2 | evatex-background-video-1.8.5-mc1.21.2.jar |
| 1.8.5+mc1.21.3 | 1.21.3 | evatex-background-video-1.8.5-mc1.21.3.jar |
| 1.8.5+mc1.21.4 | 1.21.4 | evatex-background-video-1.8.5-mc1.21.4.jar |
| 1.8.5+mc1.21.5 | 1.21.5 | evatex-background-video-1.8.5-mc1.21.5.jar |
| 1.8.5+mc1.21.6 | 1.21.6 | evatex-background-video-1.8.5-mc1.21.6.jar |
| 1.8.5+mc1.21.7 | 1.21.7 | evatex-background-video-1.8.5-mc1.21.7.jar |
| 1.8.5+mc1.21.8 | 1.21.8 | evatex-background-video-1.8.5-mc1.21.8.jar |
| 1.8.5+mc1.21.9 | 1.21.9 | evatex-background-video-1.8.5-mc1.21.9.jar |
| 1.8.5+mc1.21.10 | 1.21.10 | evatex-background-video-1.8.5-mc1.21.10.jar |
| 1.8.5+mc26.1 | 26.1 | evatex-background-video-1.8.5-mc26.1.jar |
| 1.8.5+mc26.1.1 | 26.1.1 | evatex-background-video-1.8.5-mc26.1.1.jar |
| 1.8.5+mc26.1.2 | 26.1.2 | evatex-background-video-1.8.5-mc26.1.2.jar |
| 1.8.5+mc26.2 | 26.2 | evatex-background-video-1.8.5-mc26.2.jar |
| 1.8.5+mc26.3 | 26.3 | evatex-background-video-1.8.5-mc26.3.jar |

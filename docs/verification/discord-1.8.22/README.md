# EVATEX 1.8.22 / Volt 0.7.1 Discord activity verification

The publisher EVATEX application is `1563256751623508028`. It is public configuration, not an account token. Players supply no application ID or token. The approved detailed menu black-hole PNG is bundled as the mod icon and uploaded as Discord application icon and Rich Presence asset `evatex`.

## Behavior

- With both updated mods installed: menu → EVATEX Background; world/server → Clipped with Volt; disconnect → EVATEX Background.
- With EVATEX alone, its activity remains available in menus and while playing. Both mods offer their own activity switch.
- EVATEX activity includes a View on Modrinth button, an image URL and a details URL targeting https://modrinth.com/mod/evatex-background.
- Arbitration uses the shared JVM properties under `eververity.discord.v1.`. Short property operations perform no network, file, decoder or graphics work. The previous owner clears and closes its IPC session before releasing ownership.
- Eligibility heartbeat is three seconds; background workers poll once per second, refresh an unchanged activity every twelve seconds, and back off failed connections for five seconds. Disconnected/shut-down owners expire.

## Evidence so far

- Java 17 fake Unix IPC regression: publisher handshake, READY, activity acknowledgement, ping/pong, Modrinth URLs, Volt priority, EVATEX restoration, disabling and shutdown.
- Both config implementations: existing configs default activity on; false survives save/load and copies.
- Live local desktop Discord accepted `Using EVATEX Background`, display type 2. A separate real-IPC integration run then acknowledged EVATEX menu → Volt world → EVATEX returned menu, using both actual transport classes in one JVM. This confirms IPC acceptance, not a second account’s profile view.
- Developer Portal: application icon saved; `evatex` asset persisted after navigating away and returning.

## Release gate

Compilation and package checks are recorded separately from actual game runs. The final game validation was Minecraft 1.21.11 / Fabric Loader 0.19.5, fullscreen 1920×1080, with the installed mod set. It acknowledged all three activity transitions, saved a 3.33-second H.264 clip after disconnecting and exited normally. Both tested artifacts are installed in the actual Prism profile, with previous JARs and the Atlas activity setting backed up. Complete the remaining compatibility builds/checks and wait for the user’s explicit test approval before publishing a release.

Windows and macOS runtime behavior is not verified by Linux tests. Discord activity display also depends on the user’s desktop client/activity settings. Discord can vary which activity links/buttons it exposes in each client view.

## Installed Atlas activity

The installed Atlas Client 3.0.3 has its own independent Discord publisher and its activity setting is enabled. Its publisher does not participate in this two-mod coordinator. For the requested exclusive Volt/EVATEX activity on the user’s profile, disable only Atlas’s `mod.general.name` → `setting.discord.enabled` setting, with the original config backed up. Do not change its other settings or RTC/friend-presence options.

Two earlier 1.21.11 attempts were killed with SIGKILL and generated no Java crash report; they are not counted as passes. The final run completed after stopping concurrent builds and adding a PID-scoped secondary-window test guard; these observations do not establish the cause of the earlier kills. The test harness intentionally closes the successful client at the end.

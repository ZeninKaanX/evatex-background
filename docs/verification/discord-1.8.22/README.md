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

## 2026-10-25: Flatpak export repair

The live desktop had a working Discord socket under
`$XDG_RUNTIME_DIR/.flatpak/com.discordapp.Discord/xdg-run/discord-ipc-0`,
but no exported socket under `app/com.discordapp.Discord`. The running Prism
sandbox's standard `discord-ipc-0` symlink consequently pointed to a missing
export. Both EVATEX and Volt were enabled in their existing configurations;
Atlas activity remained disabled. No Minecraft JVM was running at inspection.

- Both EVATEX source trees now search the bounded private Flatpak socket locations
  as well as standard exports. No permissions are bypassed or changed.
- The Java 17-targeted IPC regression passed with the explicit test socket and
  with automatic discovery of a private Flatpak-style socket. Both exercise
  publisher handshake, acknowledgement, ping/pong, handoff, restoration and cleanup.
- A small user service restores Discord's standard local IPC export with socat
  when it is missing. It never replaces another export; the helper source is in
  `tooling/linux-discord-bridge/bridge.py`. The same export is used by Discord's
  [official Flatpak launcher](https://github.com/flathub/com.discordapp.Discord/blob/master/discord.sh).
- The existing Prism sandbox reached the repaired export and the real Discord
  client returned READY for publisher application `1563256751623508028`.
  This is a real connection check, not another account's visual profile check.
- Full Fabric 1.21.11 build and Discord package checks passed. The rebuilt JAR
  was installed only after checking no Minecraft JVM was active; the previous
  installed JAR was backed up. Installation and handshake receipts are alongside
  this note. Neither Minecraft nor Discord was launched/restarted; no window,
  audio, activity preference or Flatpak permission was changed.
- This fix is installed locally, not published. The existing release validation
  and approval gate remains in force. In-world Volt priority is intentional.

The local helper is `%h/.local/lib/evatex/discord-ipc-bridge.py`; its unit is
`%h/.config/systemd/user/evatex-discord-ipc-bridge.service`. To remove only this
helper, run `systemctl --user disable --now evatex-discord-ipc-bridge.service`
and remove those two files, then `systemctl --user daemon-reload`. Do not remove
Discord's or Prism's own IPC paths. The helper needs only socat and Python's
standard library; it handles no account tokens and performs no network requests.

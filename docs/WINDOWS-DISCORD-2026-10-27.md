# Windows Discord live validation — 27 October 2026

## Confirmed result

A real Discord Desktop 1.0.9265 session on Windows 11 build 26100 accepted the published EVATEX 1.8.24 presence implementation. The signed-in account’s own profile visibly showed **EVATEX Background**, the black-hole asset, **Using EVATEX Background**, **Minecraft · Astral**, and the elapsed timer. No account credentials or Discord user payloads are included in this report.

The standalone probe used presence/transport classes from the published 1.8.24 artifact, application ID `1563256751623508028`, and Temurin Java 21.0.9. It connected through Windows named pipe 0. The activity acknowledgement arrived about 2.15 seconds after start with `status_display_type=2`. The pre-login attempt only reached connecting; it is not evidence of a transport failure. Activity was disabled when the two-minute probe exited.

The development implementation also received an activity acknowledgement on Windows pipe 0, with no reported IPC error. This verifies its diagnostics changes against the real client, independently of mock IPC tests.

## Evidence retained outside the source tree

Workspace: `windows-discord-live-20261027` beside the worktree.

- `results/probe-before-login.json`: unauthenticated baseline.
- `results/probe.json`: published 1.8.24 live session and disable result.
- `results/probe-dev.json`: development implementation and safe diagnostics.
- `activity-card.png`: visible profile card, SHA-256 `1b609e19fa0ddbd78b90caf4e3b82c57cfbd692b9137f79df14dcd38f4720d0c`.
- `LiveDiscordProbe.java`, `public/probe.ps1`: reproducible standalone harness.
- `tests-diagnostics.log` and `tests-legacy-diagnostics.log`: complete regression runs for Minecraft 1.21.11 / Java 25 and 1.20.1 / Java 17.

The Windows machine used a disposable QCOW2 overlay. The host desktop, host audio and main game were not controlled.

## Limits

This is a real Windows Discord client test, **not** a complete Minecraft session or a second-account visibility test. The original VM has no actual Minecraft installation. A report from a particular player can still involve an older JAR, disabled activity sharing, another presence provider, or a different integration failure. Those possibilities remain unconfirmed; this test does not identify that player’s root cause or prove universal compatibility.

## Development changes, not republished as 1.8.24

- Copyable diagnostics now include configured/enabled state, generic transport/pipe index, current-connection acknowledgement and numeric RPC error/type. They exclude account identities, server-provided error messages and filesystem paths.
- RPC rejection backs off for 30 seconds; missing activity acknowledgements time out instead of remaining silently connecting. Stale reader replies are ignored.
- Render failure immediately detaches/stops FFmpeg instead of relying on the next game recovery tick. The regression reproduced the old delayed cleanup and passes without another tick after the change.
- Chunk headers use bounded ASCII parsing rather than allocating a String and regex for each chunk. Loopback-only binding, session-token authentication and single-client admission remain intact.
- Constant meteor/icon text objects are reused.

The full decoder/IPC regressions passed in both source variants. Tiny synthetic 29.97/30/60/120 FPS timing tests verify scheduling and pause/resume; they do not benchmark Windows GPU performance at full resolution.

## External review: verified qualifications

The external review was treated as a list of hypotheses, not instructions. Recovery already closed the decoder on a later game tick; a permanent zombie until menu close was not demonstrated. Pausing both picture and audio in Backgrounds follows the user’s latest requirement. TCP buffers are finite and backpressure applies, so unbounded TCP memory growth was not established. Chunk headers already read through a buffered stream; byte-level parsing did not imply one OS call per byte. High-resolution pixel copies remain a real bandwidth cost, but a proposed 70% off-heap saving has not been measured. Thumbnail conversion, text-fitting caches and broader allocation reduction remain separate profiling work; they are not claimed fixed here.

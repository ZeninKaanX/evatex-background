# EVATEX external review — 1.8.12

## Corrected

- Added the official [YouTube showcase](https://youtube.com/shorts/EzglUlDrTkI) before the remaining review work; the live Modrinth body was verified afterward.
- Corrected Modrinth's outdated Source code link and missing issue URL. Current metadata, README support links and project links now use the active `evatex-background-video-1.8.2` repository.
- Removed outdated feature-version labels, internal Windows archive commentary and the development-story promotion from the README. Historical changelog entries remain historical. Current version metadata, filenames, compatibility table and release notes use 1.8.12.
- Replaced unauthenticated local video/PCM connections with authenticated HTTP uploads over loopback TCP. Each listener binds to IPv4 127.0.0.1 on an ephemeral port, generates a fresh 256-bit secret, bounds header size/time, rejects invalid requests and closes its listener after the authenticated upload. Closing the decoder closes active streams. This is not a security boundary against processes that can inspect the same user's arguments or memory. The [FFmpeg HTTP protocol](https://ffmpeg.org/ffmpeg-protocols.html#http) supplies streamed upload framing.
- Added a local Copy diagnostics action, English/Turkish feedback, playback issue template and voluntary physical-platform test template. Reports include OS, Java, GPU renderer, decoder, decoded output size, configured FPS and mod count. They exclude file paths, account names, raw decoder errors and connection secrets.
- Added an English full-HD vanilla-menu screenshot to the gallery without replacing the existing clean images.
- All 24 existing 1.8.11 publications already had notes. Only historical 1.4.0 and 1.7.0 lacked notes; added clearly identified archival notices without inventing historical changes.

## Verified

- The 24 published 1.8.11 jar files declare Loader >=0.18.4, except Minecraft 26.3 >=0.19.3. The current Modrinth body matches those requirements. Fabric API is not required by EVATEX itself.
- All 24 new 1.8.12 builds compile and pass package checks for exact Minecraft metadata, bytecode, embedded dependencies, engine hashes, archive integrity, FFmpeg licenses and provenance.
- Real FFmpeg playback exercises explicit hardwareAcceleration=false, native hardware initialization failure followed by software fallback, MP4/GIF loops, static photos, frame timing, borrowed buffers, pause/resume and shutdown. Physical Windows drivers are not simulated by the Linux test.
- Wrong-secret and partial-handshake clients are rejected before a valid sender succeeds. Exact chunk payload, single-connection listener closure and pending-connection shutdown pass.
- Both supplied 1080p media files additionally passed 540 authenticated software-decoded frames with live PCM reception, muted at 10%; the short Reze source loops during this check. This host-native check uses system FFmpeg.
- A real Minecraft 1.21.11 session on the secondary display verifies diagnostic copy to the actual clipboard, feedback translation, privacy fields and software playback. The original clipboard is restored. Sound stays muted at 10%.
- Existing 24-build coverage is compile/package coverage; this review does not claim 24 individual Minecraft runtime sessions.

## Not verified

- Physical macOS Intel/Apple Silicon runtime, NVIDIA/Intel/new AMD hardware decode and full proprietary Lunar Client runtime. Existing RX 580 physical evidence is recorded separately.
- The exact Windows application-control rule causing the friend's CreateProcess error 5; the logs identify permission denial, not the policy owner. The reported settings exit has no fatal stack in the supplied files.
- Search-engine cache refresh. Current first-party pages are corrected, but Google's old search snippets cannot be directly purged or made to promise an AI recollection.
- Legal compliance of combined MIT/GPL redistribution. Packaging includes engine license/provenance files; that is not a legal determination.

## Maintainer decisions

- Optional repository rename: current links are corrected without renaming. A rename requires coordinated URL/remote/CI migration.
- An opt-in preview channel should be introduced only for actual preview builds. Current releases stay stable.
- CurseForge needs a maintainer project/account and a release-mirroring choice. No upload or account change was performed.
- NeoForge first, Forge afterward: inspect a single-version proof of concept before committing a reliable schedule or cost. Shared media code can be reused, but loader entrypoints, rendering hooks and native-widget/version adapters still need porting. See [expansion plan](expansion-plan.md).

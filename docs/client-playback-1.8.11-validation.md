# EVATEX 1.8.11 client and playback validation

## Reproduced failures

The supplied Windows game log identifies Minecraft 26.1.2, Fabric Loader 0.19.5 and EVATEX 1.8.10. Bundled `ffmpeg.exe` extraction succeeds, but process creation fails with Windows error 5 (access denied). The system fallback then fails with error 2 (not found). Prior code latched into system-only mode before the fallback started, so subsequent selections never retried the bundled engine. The fix changes that state only after a system process actually starts, retains the denied-execution cause, and displays an engine-specific message even when audio is enabled.

The log also shows unsuccessful PNG selection through the same external engine. Standard photos now use ImageIO with subsampling and a bounded output size; PNG/JPEG/BMP regression fixtures publish one frame, keep the expected RGBA/ABGR channel order and launch no external decoder. GIF and video decoding still require permission to run an available FFmpeg engine. The mod does not modify Windows execution/security policies.

No fatal Java crash stack for the reported settings exit appears in the provided launcher log, message.txt or archived game log. The archived session shuts down normally; settings-crash resolution is not claimed.

Loader 0.19.3 includes Mixin Extras 0.5.4. Its actual profile reproduced a ClassCastException during window transformation, with both clean and modded profiles. Standard nested Mixin Extras 0.5.5 resolves that startup failure. Loader 0.18.4 ran Minecraft 1.21.11 through native home actions, options/world return, foreign-menu ownership and focus pause/resume using the supplied Lost Soul 1080p60 video. Package floors are 0.18.4, except Minecraft 26.3 whose embedded Fabric modules require 0.19.3.

## Actual Minecraft runtime checks

Minecraft 26.1.2 with actual Loader 0.19.3 passed a combined Linux run using the versions from the supplied log: Sodium 0.9.1, Iris 1.11.2, ImmediatelyFast 1.15.3, Rescale 1.1.0, Cloth Config 26.1.154, Mod Menu 18.0.0 and Fabric API 0.155.2. The Reze 1080p60 video played. Native Options, world/server menus, resource packs, selected server details, opt-out, late foreign-widget recovery and compact fallback passed. The optional Mod Menu factory opened EVATEX settings and returned to the same Mod Menu parent. Clean 26.1.2 Options also opened and returned successfully.

The locked desktop declined a hidden-window resize in 26.1.2. QA explicitly initialized the screen at compact logical dimensions for that check; it does not claim a physically resized framebuffer. Earlier harness failures at that check were QA assertions, not product crashes. Initial subset assembly also omitted Cloth Config; adding the dependency from the friend's profile resolved Rescale startup.

Offline authentication warnings and a missing optional asset index are present in the isolated GUI log. Runtime coverage is Linux; no physical Windows execution, Windows permission repair or full Lunar Client runtime is claimed. All sound was muted at 10%; both supplied tests used actual 1080p60 files and no 4K/8K stress playback.

## Client menu access

Public custom-widget bounds now round outwards and handle visibility, nested children, invalid values and unreadable coordinates, with bounded recursion and iteration. Generic regression tests cover these cases. Existing adaptive layout tests cover late controls, resizing and vertical shortcuts.

Lunar's official documentation says custom themes can hide native mod buttons and recommends Main Menu Style → Vanilla (Overlay): https://support.lunarclient.com/solutions/enabling-vanilla-overlay . EVATEX supplies an optional Mod Menu settings entrypoint as an additional access route. No private/obfuscated Lunar UI is modified, and a full Lunar runtime was not available locally.

## Build checks

All 24 exact-version targets compile with the matching Minecraft API and Java bytecode level. Archive checks verify metadata, embedded module bytecode, Mixin Extras, FFmpeg hashes, archive CRC and checksum sidecars. Decoder/audio regression tests and English/Turkish translation parity pass.

## Windows 11 execution-policy investigation

The supplied message.txt identifies Windows 11 and confirms successful extraction immediately before CreateProcess error 5. Microsoft defines error 5 as ERROR_ACCESS_DENIED; that code does not identify a particular security product or policy. The bundled PE's certificate-table offset and size are both zero: no embedded Authenticode signature is present. This does not check possible catalog trust or Microsoft's cloud reputation.

Smart App Control is a plausible cause because it can block unknown unsigned binaries. The friend's Smart App Control state and CodeIntegrity events were not supplied, so this remains a hypothesis alongside file permissions and other application-control or antivirus rules. Successful extraction makes protected-folder write denial a weaker explanation for this specific process-start failure. No security protections were disabled.

A separate read-only diagnostic script checks the known engine checksum before a version-only launch, reports Authenticode status and file ACLs, and reads recent matching CodeIntegrity, Defender and Security-Mitigations events. Event 3077 identifies an enforced App Control block; 3089 provides associated signature information. The script has not been executed on the friend's Windows system. A real event report is required to distinguish these causes.

Primary references:
- https://learn.microsoft.com/en-us/windows/win32/debug/system-error-codes--0-499-
- https://learn.microsoft.com/en-us/windows/apps/develop/smart-app-control/overview
- https://learn.microsoft.com/en-us/windows/security/application-security/application-control/app-control-for-business/operations/event-id-explanations

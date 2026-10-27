## 1.8.25 — Discord diagnostics and media recovery

- Add Discord connection details to Copy diagnostics: transport, acknowledgement and safe numeric failure codes, without account information or private error text.
- Back off after rejected activity requests and reconnect when an activity acknowledgement times out. Ignore replies from obsolete connections.
- Stop the video decoder immediately after rendering fails, even if the next Minecraft recovery tick cannot run.
- Reduce temporary allocations in local media chunk parsing and reuse constant Astral icon text. Keep authenticated loopback-only media transport.
- Preserve per-Minecraft Fabric Loader requirements and the previous high-FPS playback improvements.

Validation: real Windows 11 Discord Desktop displayed the EVATEX name, black-hole asset and elapsed timer using the published 1.8.24 transport; the 1.8.25 development code also received an activity acknowledgement. Full decoder/IPC regression suites passed for both 1.21.11 and 1.20.1 source variants, including immediate FFmpeg cleanup and RPC rejection backoff. This does not establish the cause of every player's missing activity, or benchmark full-resolution Windows GPU playback. See `docs/WINDOWS-DISCORD-2026-10-27.md`.

### Türkçe

- Tanı raporuna Discord bağlantısı, etkinlik onayı ve kişisel bilgi içermeyen hata kodları eklendi.
- Reddedilen bağlantılarda gereksiz tekrarlar azaltıldı; cevapsız etkinlik gönderiminde yeniden bağlanma eklendi.
- Görüntüleme hatasında FFmpeg hemen kapatılır; bazı geçici bellek tahsisleri azaltıldı.
- Gerçek Windows 11 Discord profilinde EVATEX adı, karadelik simgesi ve süre sayacı doğrulandı. Her oyuncunun kurulumundaki sorunun aynı nedenden kaynaklandığı iddia edilmez.

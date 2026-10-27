## 1.8.24 — Windows Discord activity

- Avoid an empty blocking read on the synchronous Windows Discord pipe. The reader checks buffered bytes using the Windows JDK's `PeekNamedPipe` path before reading; idle reads no longer hold up activity publication or clearing.
- Keep Windows pipe reads and writes serialized only while data is ready, and close the idle reader promptly when activity is disabled. Reconnect remains on the daemon worker.
- Distinguish Windows from Darwin when choosing IPC transport; macOS continues to use Unix sockets.
- Retain the high-FPS frame handoff and per-Minecraft Fabric Loader fixes from 1.8.23.

Validation: isolated native Win32 pipe regression covers fragmented READY packets, concurrent publication, acknowledgements, ping/pong, clearing and reconnect. The test uses a unique local pipe, never a user's Discord account. Native Windows CI passed: run 40427109564 (commit 12c6668). Physical Windows Discord UI and GPU playback still need player confirmation; Discord activity privacy settings remain under the player's control.

### Türkçe

- Windows Discord bağlantısında boş boruyu bekleyen okumanın etkinlik göndermeyi engellemesi giderildi.
- Etkinliği kapatma ve yeniden bağlanma davranışı gerçek Windows named pipe testiyle kontrol edilir.
- 1.8.23 içindeki FPS ve Fabric Loader düzeltmeleri korunur.

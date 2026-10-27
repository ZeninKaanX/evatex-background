## 1.8.26 — External video engine for CurseForge

- Add a CurseForge distribution containing no bundled executable or compressed engine. Videos and animated GIFs use an external FFmpeg engine; photos need no engine.
- Offer explicit, one-time **Download engine** setup when applying a video, with progress, retry and Back. The download does not start at mod startup or just by opening the setup screen.
- Download pinned upstream builds over HTTPS with host/redirect checks, bounded downloads, archive and binary SHA-256 verification, exact-entry extraction and partial-file cleanup. Cache successful installations; support user-managed FFmpeg and verified engines from older EVATEX installs.
- Keep normal Modrinth bundled-engine behavior. Add engine setup to Playback settings in the external-engine distribution.
- Check the entire CurseForge package, including nested JARs, for executable payloads before submission.

CurseForge approval is separate from successful upload. A corrected first package is submitted for moderation before the remaining compatibility matrix. No approval is implied by these notes.

### Türkçe

- CurseForge paketi artık çalıştırılabilir FFmpeg dosyası içermez. Video/GIF için motor oyun içindeki **Motoru indir** düğmesiyle bir kez kurulabilir.
- İndirme kullanıcı düğmeye bastıktan sonra arka planda başlar; dosya ve motor SHA-256 ile doğrulanır. Sonraki açılışlarda yeniden indirilmez.
- Bağlantı hatasında tekrar deneme vardır; fotoğraflar motorsuz çalışır. Modrinth paketlerinin mevcut motor davranışı korunur.
- Moderasyon onayı, yükleme durumundan ayrı takip edilir.

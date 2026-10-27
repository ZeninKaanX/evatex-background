## 1.8.23 — High-FPS playback and Fabric Loader compatibility

- Treat the video FPS preference as a ceiling. A 30/60 FPS source no longer generates 120 duplicate full RGBA frames per second when the ceiling is 120; native 120 FPS sources remain supported.
- Use a bounded three-slot latest-frame handoff between the decoder and renderer. Pixel copies and GPU uploads no longer hold the decoder publication lock; superseded pending frames are recycled without a backlog.
- Compile each Fabric adapter against its advertised minimum Loader. Older Minecraft releases no longer inherit the 1.21.8 Loader floor. Embedded resource modules and MixinExtras are still checked against each declared minimum.
- Report effective video FPS and superseded frames in playback diagnostics.
- Include the English 16:9 Echidna & Subaru showcase in project descriptions.

Performance still depends on source resolution, codec, CPU, GPU and driver. This update does not claim zero rendering cost. Fabric 1.21.11 continues to require Loader 0.17.3+ because its bundled resource modules require it; requirements from other mods remain independent.

### Türkçe

- FPS ayarı üst sınır olarak çalışır; 30/60 FPS videolar 120 kareye gereksiz yere çoğaltılmaz. Gerçek 120 FPS videolar desteklenir.
- Video çözme ve çizim arasında sabit boyutlu üç tampon kullanılır; piksel kopyaları ve ekran kartına aktarma sırasında çözücü kilitlenmez.
- Eski Minecraft sürümlerine gereksiz yeni Fabric Loader sınırı uygulanmaz. İç bağımlılıklar ve bildirilen alt sınır birlikte kontrol edilir.
- Tanı raporunda gerçek video FPS değeri ve yerine daha yeni kare gelen görüntülerin sayısı yer alır.

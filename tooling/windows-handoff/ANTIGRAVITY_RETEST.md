# Windows continuation — 1.8.5-beta.1

Mevcut Evatex klasöründen ve mevcut videodan devam et; kullanıcıdan yeniden paket isteme.
Gönderilen evatex-results.zip, önceki paketteki yedi dosyanın aynısı. Yeni ölçüm değil.
GPU Video Decode çalıştığı doğrulandı, fakat gerçek Minecraft/Atlas akıcılığı ve
önce/sonra kazancı henüz doğrulanmadı. Başsız test sonuçlarını oyun FPS'si diye sunma.

Repo: https://github.com/ZeninKaanX/evatex-background-video-1.8.2
Güncel main sürümü: 1.8.5-beta.1. Önce git durumunu incele; yerel değişiklikleri,
eski raporları ve baseline jarını koru. Gerekirse ayrı çalışma ağacı kullan.
Yeni kaynakta D3D11 width/height düzeltmesi ve Windows hareketli medyada bicubic
zaten var; eski patch'i tekrar uygulama. Varsayım üzerinden yeni optimizasyon ekleme.

1. Windows JDK 21, Python ve Git kurulu mu kontrol et. JAVA_HOME doğru JDK olsun.
   `python scripts/build.py --minecraft 1.21.11` ile aday jarı derle.
   Bundled FFmpeg gerekiyorsa scripts/fetch-ffmpeg.sh içindeki sabit kaynak ve
   checksumları takip et; kaynak jarı işletim sistemi FFmpeg'siyle karıştırma.
2. Mevcut Reze videosunu kullan. SHA256:
   b45865b81fb72c1aa2300eb1ac487577cf18dbbec9c22b7a9d55128d640eea1e
   Bu kaynak 1920×1080, 60 FPS. 4K/8K oynatma veya üretme.
3. Başsız ölçüm: `python tooling/windows_benchmark.py --video '<mevcut-video-yolu>' --seconds 60 --repeats 3`.
   Baseline ve aday için ayrı çıktı dizinleri kullan; script --help ile çıktı
   seçeneğini doğrula. Her koşulda aynı kaynak/ayarlarla ham raporları sakla.
4. Gerçek Minecraft 1.21.11 + Fabric 0.19.5+ oyununu izole test profilinde aç.
   Evatex dışında performans modları ekleme. Mevcut Atlas Client 3.0.3 ve
   bağımlılıkları ile ayrı Atlas koşulunu da ölç. Kullanıcı ana profilini değiştirme.
   Ses açılmadan önce audioVolume=0.1, audioEnabled=false olsun; sesi açma.
   Yan ekran varsa oyunu oraya taşı; yoksa bu eksikliği raporla.
5. Normal Minecraft ve Atlas'ta aynı video/ayarlarla baseline ve aday için
   60 saniye × 3 tekrar yap. collect-performance.ps1 ile gerçek Java PID'sinin
   CPU'sunu, FFmpeg CPU'sunu ve GPU motorlarını kaydet. Oyun FPS/kare sürelerini
   ayrıca ölç; cihaz sayacı yoksa bilinmiyor yaz. Boş GPU sayacını sıfır diye yorumlama.
6. Yeni menüler: dünya seçimi, ana menü ayarları ve ses ayarlarında video sürsün,
   hafif karartma ve meteorlar görünsün. EVATEX'teki Dünya seçimi/Ayarlar menüleri/
   Menü meteorları düğmelerini ayrı ayrı kapat-aç. Video menü geçişinde yeniden
   başlamasın. Dünya içinden açılan ayarlar normal oyun arka planını korusun.
7. Yeni sonuç paketine baseline/candidate ham ölçümleri, her koşulun ayarlarını,
   jar SHA256 ve commit'i, temizlenmiş gerçek oyun latest.log dosyalarını,
   ekran görüntülerini ve açık eksikleri ekle. Ölçmediğin şeyi tamamlandı yazma.
   opengl_tested=true yalnızca gerçek oyun oynatması yapıldıysa kullanılabilir.
   Önceki yedi dosyayı yeni sonuç diye tekrar paketleme. Henüz Modrinth'e yayımlama.

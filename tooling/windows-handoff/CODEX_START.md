# Windows Codex devralma talimatı

EVATEX-Background 1.8.4'ün gerçek Windows/GPU performansını ölç ve kasmanın nedenini bul.
Linux'taki Codex kodu ve yayınları yönetiyor. Sen Windows doğrulamasını üstlen.
Mevcut Linux görevine otomatik erişimin olduğunu varsayma. Sonuçları aşağıdaki
rapora yaz; kullanıcı bu raporu Linux görevine iletecek. Kimlik bilgisi gerekmiyor.

Repo: https://github.com/ZeninKaanX/evatex-background-video-1.8.2
Önce `tooling/windows-handoff/README.md` ve test manifestini oku.
Mevcut ürün kaynağı e9717dd, doğrulanmış 1.8.4 yayınındaki dosya aşağıdaki manifestte.
09af286 test altyapısı ekledi; üretim davranışını değiştirmedi.

Windows'ta yerel PowerShell + Windows Java + Windows FFmpeg kullan; WSL/Wine ile
ölçüm yapma. Araç eksikse uygun kurulum yöntemini kullanıcı ortamına göre çöz.
İlk iş donanımı, ekranları, Minecraft 1.21.11 için Java 21'i ve Fabric Loader
en az 0.19.5'i doğrula. C2ME'nin Java 25 isteyen sürümü varsa bu çakışmayı gider;
bunu EVATEX performans sorunu ile karıştırma.

Prism'de test için ayrı/kopya profil kullan. Kullanıcının ana profilini değiştirme.
Yerel örnek modun evatexbackgroundvideo kimliğini koru; Atlas ayrı mod olarak kalsın.
Oyun penceresini sadece yan/ana olmayan ekranda aç. Başlangıç dahil bu konumu
garanti edemiyorsan GUI testine başlamadan kullanıcıdan yan ekranı belirlemesini iste.
Tek ekran varsa başsız testleri yap, GUI koşulu için kullanıcı yönlendirmesini bekle.
Ses açılmadan önce %10'a ayarlanmalı; test sırasında %10 üstüne çıkma.
4K/8K video oynatma veya üretme. Kullanıcının gönderdiği Reze dosyasının adında
4K yazsa da Linux'ta doğrulanmış gerçek çözünürlük 1920x1080'dur. Windows'a kopyalanan
dosyanın çözünürlüğünü ayrıca doğrula. Video dosyasını GitHub'a yükleme.

1. Manifestteki mod jarını SHA512 doğrulayarak ayrı profile kur. Test ayarlarını
   kaydet: video özeti/SHA256, FPS=30, ölçek=1, maxDecodeHeight=1080, donanım
   hızlandırma açık, ses kapalı ve %10, pencere 1280x720, Minecraft FPS sınırı 60.
2. Önce normal Minecraft+EVATEX, sonra aynı ayarlarla Atlas 3.0.3+EVATEX dene.
   C2ME ve diğer optimizasyon modlarını ilk ölçümden çıkar. Atlas jarını Linux
   kullanıcısı ayrıca kopyalayacak; repo/paket Atlas içermez.
3. Başlangıç/loop/kare takılmaları ve gerçek oyun FPS/kare sürelerini kaydet.
   PresentMon gibi mevcut ölçüm aracı kullanılabilir; Minecraft FPS ile video
   FPS'i ayrı belirt. Sadece FFmpeg ölçümünü oyun ölçümü olarak sunma.
4. Gerçek oyun Java PID'siyle collect-performance.ps1 kullan. Çözücü FFmpeg ve
   Minecraft CPU'sunu ayrı incele; GPU Video Decode, Copy ve 3D motorlarını izle.
   Görev Yöneticisi toplam GPU grafiği tek başına donanım çözmeyi kanıtlamaz.
   latest.log içinden `EVATEX video decoder active` ve `D3D11 ... unavailable/stopped`
   satırlarını al. Etkin arka uç D3D11_SCALE, D3D11 veya SOFTWARE olabilir.
5. Hızlandırmayı kapatıp aynı testi tekrarla. Sonra 720p hedefiyle tekrarla.
   Her seferinde tek ayarı değiştir, en az 60 saniye ve bir tam döngü izle.
6. Gerekirse repo scripts/test_desktop.py ve tooling/windows_benchmark.py çalıştır.
   Bunlar gerçek GPU/Minecraft GUI ölçümüne ek kontrollerdir.
7. Kanıt olmadan sürücü/PC'yi suçlama veya 'kusursuz düzeldi' deme. GPU yolu
   başarısızsa FFmpeg'in somut hata satırını tespit et. Yalnız kanıtlanan nedeni
   çöz, aynı donanımda önce/sonra ölç. Kod değişecekse ayrı yerel Windows dalı
   kullan, main'e veya Modrinth'e buradan yayın yapma.

Sonuç: `WINDOWS_RESULT.md`, `hardware.json`, `processes.csv`, `gpu-engines.csv`,
yalnız ilgili hata/decoder satırları, varsa kare süresi ölçümü ve başsız benchmark
report.json. Ham loglardaki sunucu adresleri, sohbet, kullanıcı adı/ev dizini gibi
kişisel bilgileri rapora koyma. Token, launcher accessToken, oturum verileri veya
özel anahtarları kopyalama. Kullanıcının mevcut modları/hesaplarını silme.

Raporda CPU/GPU modeli, sürücüsü, Java/MC/Fabric sürümü, jar/video hash'i, ayarlar,
gerçek arka uç, normal/Atlas karşılaştırması, FPS/kare süreleri, ayrı CPU oranları,
GPU motorları, ilk kare/loop/ses davranışı, hata ve varsa önce/sonra sonuçları bulunsun.
Linux tarafındaki Codex bu rapor ve patch üzerinden devam edecek.

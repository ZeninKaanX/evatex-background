# EVATEX Windows test paketi

Bu paket ölçüm ve işi devralma içindir. Mod dosyası içermez; doğrulanacak 1.21.11
jarının yayın bilgileri manifest.json'da bulunur. Hesap/token gerekmez.

Windows'ta bir klasöre çıkar, klasörü Codex'te aç ve CODEX_START.md talimatını ver.
Yanına Linux'ta kullanılan 1080p Reze videosunu kopyala. Atlas kıyaslaması için
Atlas Client 3.0.3 Minecraft 1.21.11 jarını ayrıca kopyala. Bu dosyalar herkese
açık pakete dahil edilmez. Ana olmayan ekran koşulunu Codex'e ayrıca belirt.

## CPU ve GPU ölçümü

Oyun açıkken gerçek oyun Java PID'sini belirle, PowerShell'de:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\collect-performance.ps1 -GamePid 12345 -Seconds 60 -OutputDirectory .\results-normal-1080p
```

12345 yerine gerçek Java PID'si gerekir. Script ayar/mod değiştirmez, ses açmaz,
pencere açmaz. Oyun CPU ve doğrudan FFmpeg çocuk süreçleri ayrı örneklenir.
GPU motorları ayrıca örneklenir. Sayaç yoksa eksik veri olarak raporlanır.
CPU tüm mantıksal işlemcilere göre yüzde; GPU motorlarını toplayarak tek toplam
yüzde üretme. Sayacın olmadığı durum GPU kullanımının sıfır olduğunu göstermez.

## Başsız çözücü ölçümü (Codex için)

Repo HTTPS klonlanabilir; Windows Git/Python/JDK 21 gerekir. JAVA_HOME Windows
JDK 21'e işaret etmeli. Mevcut ana profile dokunmadan repo kökünde:

```powershell
python scripts/test_desktop.py
python tooling/windows_benchmark.py --video 'C:\Evatex-Test\Reze_1080p60.mp4'
```

Kaynak 1920x1080 üstündeyse ikinci komut oynatmayı reddeder. Video parametresi
olmazsa 4 saniyelik sessiz sentetik 1080p60 üretir. Ölçüm üretim VideoDecoder,
kare paketleme ve iki kopyayı kapsar; OpenGL yüklemesini kapsamaz. Sonuç
build/windows-performance/report.json. GUI testi ayrıca yapılmalıdır.

## Birlikte çalışma

Linux görevinde ürün değişikliği ve yayın; Windows görevinde gerçek donanım testi.
Önce 1.8.4 baseline, sonra tek değişiklik, sonra aynı ayarla yeniden ölçüm.
Yeni Windows görevinin Linux görevini otomatik okuyacağını varsayma.
WINDOWS_RESULT.md ve ölçüm dosyalarını Linux görevine geri aktar; erişilebilir
özel dosya paylaşımı da kullanılabilir. Sonuçların paylaşımı kullanıcı aracılığıyla
yapılır; iki hesap oturumu arasında otomatik mesajlaşma kurulmaz.

import com.videomenu.VideoMenuConfig;
import com.videomenu.VideoFiles;
import com.videomenu.AstralTheme;
import com.videomenu.video.VideoDecoder;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DecoderRegression {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void writeTimedGif(Path path) throws Exception {
        var writer=javax.imageio.ImageIO.getImageWritersByFormatName("gif").next();
        try(var output=javax.imageio.ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(output);writer.prepareWriteSequence(null);
            int[] colours={0xffff0000,0xff00ff00,0xff0000ff},delays={10,30,60};
            for(int i=0;i<3;i++) {
                var image=new java.awt.image.BufferedImage(160,90,java.awt.image.BufferedImage.TYPE_INT_RGB);
                int[] pixels=new int[160*90];Arrays.fill(pixels,colours[i]);image.setRGB(0,0,160,90,pixels,0,160);
                var metadata=writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
                String format=metadata.getNativeMetadataFormatName();
                var tree=(javax.imageio.metadata.IIOMetadataNode)metadata.getAsTree(format);
                var control=(javax.imageio.metadata.IIOMetadataNode)tree.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("delayTime",Integer.toString(delays[i]));metadata.setFromTree(format,tree);
                writer.writeToSequence(new javax.imageio.IIOImage(image,null,metadata),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        } finally {writer.dispose();}
    }
    public static void main(String[] args) throws Exception {
        VideoMenuConfig invalid = new VideoMenuConfig();
        invalid.fps = -100; invalid.scale = Double.NaN; invalid.darkening = Double.POSITIVE_INFINITY; invalid.videoPath = null;
        invalid.validate();
        check(invalid.fps == 15 && invalid.scale == 1 && invalid.darkening == .25 && invalid.videoPath.isEmpty(), "invalid config normalization");
        check(AstralTheme.blend(0x00000000,0xFFFFFFFF,.5)==0x80808080,"palette interpolation");
        invalid.fps = Integer.MAX_VALUE; invalid.scale = 50; invalid.darkening = -9; invalid.validate();
        check(invalid.fps == 120 && invalid.scale == 1 && invalid.darkening == 0, "config upper/lower bounds");
        Path unicodeFolder=Path.of("videos","Türkçe [clip]").toAbsolutePath();
        check(com.videomenu.FolderOpener.commands("Windows 11",unicodeFolder).get(0).equals(java.util.List.of("explorer.exe",unicodeFolder.toString())),"Windows opener keeps path as one argument");
        check(com.videomenu.FolderOpener.commands("Darwin",unicodeFolder).get(0).get(0).equals("open"),"Darwin routes to macOS opener");
        check(com.videomenu.FolderOpener.commands("Linux",unicodeFolder).get(0).equals(java.util.List.of("xdg-open",unicodeFolder.toString())),"Linux opener keeps Unicode/spaces");
        invalid.audioVolume=Double.NaN;invalid.validate();check(invalid.audioVolume==1,"invalid audio gain normalized");
        invalid.audioVolume=-1;invalid.validate();check(invalid.audioVolume==0,"negative audio gain clamped");
        invalid.audioVolume=9;invalid.validate();check(invalid.audioVolume==1,"audio gain capped");
        var gain=com.videomenu.video.VideoAudio.class.getDeclaredMethod("applyVolume",byte[].class,int.class,double.class);gain.setAccessible(true);
        byte[] pcm={(byte)0xff,0x7f,0,(byte)0x80};gain.invoke(null,pcm,4,.5);
        check(java.nio.ByteBuffer.wrap(pcm).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort()==16384,"positive PCM gain");
        check(java.nio.ByteBuffer.wrap(pcm).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort(2)==-16384,"negative PCM gain");
        gain.invoke(null,pcm,4,0.0);check(java.util.Arrays.equals(pcm,new byte[4]),"zero gain silences PCM");
        Method engineFailure=VideoDecoder.class.getDeclaredMethod("engineFailure",Throwable.class);engineFailure.setAccessible(true);
        var blocked=new java.io.IOException("CreateProcess error=5, Erişim engellendi");
        var missing=new java.io.IOException("Cannot run program ffmpeg: CreateProcess error=2");missing.addSuppressed(blocked);
        check(engineFailure.invoke(null,missing).equals("engine_blocked"),"permission denial remains visible after missing fallback");
        check(engineFailure.invoke(null,new java.io.IOException("Cannot run program ffmpeg: CreateProcess error=2")).equals("engine_missing"),"missing executable status");
        Method backends = VideoDecoder.class.getDeclaredMethod("backends", String.class, boolean.class);
        backends.setAccessible(true);
        var windowsBackends = (java.util.List<?>) backends.invoke(null,"Windows 11",true);
        check(windowsBackends.toString().equals("[D3D11_SCALE, D3D11, SOFTWARE]"),"Windows GPU preference and bounded fallback");
        check(backends.invoke(null,"Darwin",true).toString().equals("[SOFTWARE]"),"Darwin is not mistaken for Windows");
        check(backends.invoke(null,"Linux",true).toString().equals("[SOFTWARE]"),"existing Linux decoding path preserved");
        check(backends.invoke(null,"Windows 11",false).toString().equals("[SOFTWARE]"),"hardware acceleration opt-out");
        invalid.hardwareAcceleration=false;
        check(!invalid.copy().hardwareAcceleration,"hardware preference survives config copy");
        check(VideoFiles.resolve("invalid\0path") == null, "invalid path rejected");
        Method pack = VideoDecoder.class.getDeclaredMethod("pack", byte[].class, int.class, int.class, int[].class);
        pack.setAccessible(true);
        int[] packed = new int[2];
        pack.invoke(null, new byte[]{(byte)255,0,0,(byte)255,0,0,(byte)255,(byte)255}, 2,1,packed);
        check(packed[0] == 0xFF0000FF && packed[1] == 0xFFFF0000, "RGBA -> native ABGR channel order");
        Method target = VideoDecoder.class.getDeclaredMethod("computeTarget", int.class,int.class); target.setAccessible(true);
        VideoMenuConfig config = new VideoMenuConfig();
        check(!config.customMainMenu,"optional Astral home is disabled by default");
        config.customMainMenu=true;check(config.copy().customMainMenu,"optional Astral home survives config copy");
        config.customMainMenu=false;
        VideoDecoder sizing = new VideoDecoder(config, (frame,w,h) -> {});
        int[] size = (int[])target.invoke(sizing,7680,4320);
        check(size[0] == 1920 && size[1] == 1080,"8K source safely targets 1080p without allocating or decoding 8K");
        size = (int[])target.invoke(sizing,1080,1920);
        check(size[0] == 608 && size[1] == 1080,"portrait aspect ratio within safe height");
        size=(int[])target.invoke(sizing,3840,2160);
        check(size[0]==1920&&size[1]==1080,"4K dimension-only policy");
        String originalOs=System.getProperty("os.name");
        try {
            System.setProperty("os.name","Windows 11");
            int[][] cases={{1920,1080,1280,720,1,1280,720},{1920,1080,640,480,1,852,480},
                {1920,1080,640,480,0,640,360},{1080,1920,1280,720,1,608,1080},
                {1080,1920,1280,720,0,404,720},{1920,1080,0,0,1,1920,1080}};
            for(int[] c:cases) {
                config.fillScreen=c[4]==1;
                var viewport=new VideoDecoder(config,(frame,w,h)->{},()->{},true,c[2],c[3]);
                size=(int[])target.invoke(viewport,c[0],c[1]);
                check(size[0]==c[5] && size[1]==c[6],"Windows viewport fill/fit sizing "+Arrays.toString(c));
            }
            config.fillScreen=true;
            var photoSizing=new VideoDecoder(config,(frame,w,h)->{},()->{},true,1280,720);
            var imageField=VideoDecoder.class.getDeclaredField("stillImage");imageField.setAccessible(true);imageField.setBoolean(photoSizing,true);
            size=(int[])target.invoke(photoSizing,1920,1080);check(size[0]==1920 && size[1]==1080,"still image keeps configured detail");
        } finally {System.setProperty("os.name",originalOs);}
        System.out.println("PASS Windows framebuffer sizing, portrait/crop detail, headless and still-image exclusions");
        for(int w:new int[]{280,320,427,640,1920})for(int h:new int[]{200,240,270,360,720}) {
            var layout=com.videomenu.AstralLayout.of(w,h);
            check(layout.left()>=0 && layout.right()+layout.colWidth()<=w,"layout stays within width");
            check(layout.top()+(layout.rows()-1)*layout.step()+layout.step()-2<layout.pagerY(),"controls do not overlap pagination");
            check(layout.pagerY()+18<layout.statusY() && layout.statusY()+9<layout.footerY(),"separate pager/status/footer rows");
        }
        check(!new VideoDecoder(config,(frame,w,h)->{}).start(),"empty video gracefully rejected");
        for (int fps : new int[]{15,60}) {
            config.videoPath = Path.of(args[0]).toAbsolutePath().toString(); config.fps = fps;
            CountDownLatch enough = new CountDownLatch(1);
            AtomicInteger count = new AtomicInteger();
            int[][] first = new int[1][];
            int[][] snapshot = new int[1][];
            long[] firstAt = new long[1], lastAt = new long[1];
            java.util.List<Long> times=new java.util.concurrent.CopyOnWriteArrayList<>();
            VideoDecoder decoder = new VideoDecoder(config,(frame,w,h)-> {
                check(w==160 && h==90,"native resolution preserved");
                int n=count.incrementAndGet();times.add(System.nanoTime());
                if (n==1) { first[0]=frame; snapshot[0]=frame.clone(); firstAt[0]=System.nanoTime(); }
                if (n==fps*3+1) { lastAt[0]=System.nanoTime(); enough.countDown(); }
            });
            check(decoder.start(),"decoder starts");
            check(enough.await(10, TimeUnit.SECONDS),"video loops past clip duration");
            decoder.close();
            double elapsed = (lastAt[0]-firstAt[0])/1e9;
            if(elapsed>=4.5) {
                var gaps=new java.util.ArrayList<Double>();for(int i=1;i<times.size();i++)gaps.add((times.get(i)-times.get(i-1))/1e6);
                gaps.sort(java.util.Comparator.reverseOrder());System.out.println("Decoder timing diagnostics fps="+fps+" largest frame gaps (ms)="+gaps.subList(0,Math.min(10,gaps.size()))+" ffmpeg="+decoder.lastError());
            }
            check(elapsed >= 2.75 && elapsed < 4.5,"three video seconds at "+fps+" FPS: "+elapsed);
            check(Arrays.equals(first[0],snapshot[0]),"published frame remains immutable");
            Thread.sleep(200);
            long closedCount=decoder.publishedFrames(); Thread.sleep(200);
            check(closedCount==decoder.publishedFrames(),"close stops publishing");
            System.out.printf("PASS %d FPS: %.3fs, loop, immutable frames, shutdown%n",fps,elapsed);
        }

        for(boolean tcp:new boolean[]{false,true}) {
            config.videoPath=Path.of(args[0]).toAbsolutePath().toString();config.fps=30;
            VideoDecoder pausedDecoder=new VideoDecoder(config,(frame,w,h)->{});
            var socketField=VideoDecoder.class.getDeclaredField("socketTransport");socketField.setAccessible(true);socketField.setBoolean(pausedDecoder,tcp);
            check(pausedDecoder.start(),"pause fixture starts");
            long until=System.nanoTime()+5_000_000_000L;
            while(pausedDecoder.publishedFrames()<5 && System.nanoTime()<until)Thread.sleep(20);
            check(pausedDecoder.publishedFrames()>=5,"pause fixture plays");
            var processField=VideoDecoder.class.getDeclaredField("process");processField.setAccessible(true);
            Process process=(Process)processField.get(pausedDecoder);
            pausedDecoder.setPaused(true);Thread.sleep(300);
            long settled=pausedDecoder.publishedFrames();
            var cpuBefore=process.info().totalCpuDuration();Thread.sleep(800);
            check(pausedDecoder.isPaused() && settled==pausedDecoder.publishedFrames(),"no frames produced while unfocused");
            var cpuAfter=process.info().totalCpuDuration();
            if(cpuBefore.isPresent() && cpuAfter.isPresent())System.out.println("PAUSE ffmpeg CPU ms/800ms="+cpuAfter.get().minus(cpuBefore.get()).toMillis()+" TCP="+tcp);
            pausedDecoder.setPaused(false);until=System.nanoTime()+3_000_000_000L;
            while(pausedDecoder.publishedFrames()<=settled+3 && System.nanoTime()<until)Thread.sleep(20);
            check(pausedDecoder.publishedFrames()>settled+3 && processField.get(pausedDecoder)==process,"resume without restarting decoder");
            pausedDecoder.setPaused(true);pausedDecoder.close();Thread.sleep(250);
            check(!process.isAlive(),"close wakes paused decoder and terminates ffmpeg");
            System.out.println("PASS focus pause/resume, same process and paused shutdown; TCP="+tcp);
        }
        var transport=VideoDecoder.class.getDeclaredField("socketTransport");transport.setAccessible(true);
        // Still images upload once, leave no ongoing decoder process, and do not
        // switch back to the panorama when a successful single-frame stream ends.
        for (String extension : new String[]{"png","jpg","bmp"}) {
            Path image=Path.of(args[0]).toAbsolutePath().getParent().resolve("photo %03d Türkçe."+extension);
            check(VideoFiles.isMedia(image) && VideoFiles.isStillImage(image),"image discovery " + extension);
            config.videoPath=image.toString();
            CountDownLatch shown=new CountDownLatch(1);
            AtomicInteger failed=new AtomicInteger(), imageCount=new AtomicInteger();
            VideoDecoder photo=new VideoDecoder(config,(frame,w,h)->{check(w==160&&h==90,"photo dimensions");imageCount.incrementAndGet();shown.countDown();},failed::incrementAndGet,true);
            transport.setBoolean(photo,true);
            check(photo.start() && shown.await(10,TimeUnit.SECONDS),"photo decodes " + extension);
            var backend=VideoDecoder.class.getDeclaredField("decoderBackend");backend.setAccessible(true);
            check(backend.get(photo).equals("IMAGE_IO"),"standard photo must not launch FFmpeg");
            Thread.sleep(500);
            check(photo.publishedFrames()==1 && imageCount.get()==1 && failed.get()==0,"photo stays visible without repeated decoding");
            var process=VideoDecoder.class.getDeclaredField("process");process.setAccessible(true);
            check(process.get(photo)==null,"photo process exits after upload");photo.close();
        }
        config.videoPath=Path.of(args[0]).toAbsolutePath().getParent().resolve("animation.gif").toString();config.fps=30;
        writeTimedGif(Path.of(config.videoPath));
        check(VideoFiles.isGif(Path.of(config.videoPath)),"GIF discovery");
        CountDownLatch loopedGif=new CountDownLatch(65);
        java.util.concurrent.ConcurrentHashMap<Integer,Integer> gifColours=new java.util.concurrent.ConcurrentHashMap<>();
        long gifStart=System.nanoTime();
        VideoDecoder animation=new VideoDecoder(config,(frame,w,h)->{gifColours.merge(frame[0],1,Integer::sum);loopedGif.countDown();},()->{},true);
        check(animation.start() && loopedGif.await(10,TimeUnit.SECONDS),"GIF loops beyond its source duration");
        check(gifColours.size()==3 && (System.nanoTime()-gifStart)>1_900_000_000L,"GIF content changes at paced speed");animation.close();
        var durations=new java.util.ArrayList<>(gifColours.values());durations.sort(Integer::compareTo);
        check(durations.get(0)<durations.get(1) && durations.get(1)<durations.get(2),"GIF variable source delays preserved");
        // Force the Windows transport on every CI host. Callback-owned arrays are
        // recycled after return, with a bounded pool even across clip loops.
        config.videoPath=Path.of(args[0]).toAbsolutePath().toString();config.fps=60;
        CountDownLatch pooledFrames=new CountDownLatch(121);
        java.util.Set<int[]> identities=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        VideoDecoder pooled=new VideoDecoder(config,(frame,w,h)->{identities.add(frame);pooledFrames.countDown();},()->{},true);
        transport.setBoolean(pooled,true);
        check(pooled.start() && pooledFrames.await(10,TimeUnit.SECONDS),"local TCP transport plays and loops");
        pooled.close();Thread.sleep(300);
        check(identities.size()<=6,"no per-frame large array allocation in borrowed mode");
        var server=VideoDecoder.class.getDeclaredField("videoServer");server.setAccessible(true);
        check(server.get(pooled)==null,"video socket closes on shutdown");
        System.out.println("PASS PNG/JPEG/BMP single decode, literal Unicode/percent paths, GIF loops/timing, TCP transport and bounded frame pool");
        // Exercise failed native GPU initialization followed by real software
        // decoding on GPU-less CI hosts; all process/audio resources must be closed.
        config.videoPath=Path.of(args[0]).toAbsolutePath().toString(); config.fps=30;
        CountDownLatch fallbackFrames=new CountDownLatch(12);
        VideoDecoder fallback=new VideoDecoder(config,(frame,w,h)->fallbackFrames.countDown());
        var candidates=VideoDecoder.class.getDeclaredField("backendCandidates");candidates.setAccessible(true);
        candidates.set(fallback,windowsBackends);
        transport.setBoolean(fallback,true);
        check(fallback.start(),"hardware fallback decoder starts");
        check(fallbackFrames.await(35,TimeUnit.SECONDS),"unsupported GPU paths reach playable software fallback");
        check(!fallback.decoderBackend().startsWith("starting "),"backend is marked active only after decoded frame");
        fallback.close();Thread.sleep(300);
        check(!Thread.getAllStackTraces().keySet().stream().anyMatch(t->t.isAlive()&&t.getName().equals("EVATEX-Decoder-Startup")),"first-frame watchdog closes after playback/shutdown");
        System.out.println("PASS hardware candidate policy, native-device fallback and startup cleanup");
        System.out.println("PASS config bounds, invalid paths, color ordering, safe dimension policy (no 4K/8K playback), responsive layout");
    }
}

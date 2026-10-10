package com.videomenu;
import java.nio.charset.StandardCharsets;
import java.util.List;
/** Own additions only; Minecraft and resource-pack splash texts remain intact. */
public final class SplashMessages {
    private SplashMessages() {}
    private static final List<String> MESSAGES=load();
    public static List<String> all() {return MESSAGES;}
    private static List<String> load() {
        try(var input=SplashMessages.class.getClassLoader().getResourceAsStream("assets/evatexbackgroundvideo/texts/splashes.txt")) {
            if(input==null)return List.of();
            return new String(input.readAllBytes(),StandardCharsets.UTF_8).lines().map(String::strip).filter(s->!s.isEmpty()).distinct().toList();
        }catch(Exception e){VideoMenuClient.LOGGER.warn("Cannot load EVATEX splash texts",e);return List.of();}
    }
}

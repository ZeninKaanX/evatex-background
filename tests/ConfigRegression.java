import com.videomenu.VideoMenuConfig;
import java.nio.file.*;
public final class ConfigRegression {
 public static void main(String[] args)throws Exception {
  Path file=Path.of(System.getProperty("evatex.test.dir"),VideoMenuConfig.FILE_NAME);
  Files.createDirectories(file.getParent());
  Files.writeString(file,"{\"fps\":60,\"inGameMenuBackground\":true,\"libraryFavorites\":[\"space-one\",null,{},[],true,123,\"space-one\",\"invalid id\",\"anime-two\"],\"libraryCatalogUrl\":{}}");
  var cfg=VideoMenuConfig.load();
  if(cfg.fps!=60||!cfg.inGameMenuBackground||!cfg.libraryCatalogUrl.isEmpty()||!cfg.libraryFavorites.equals(java.util.List.of("space-one","anime-two")))throw new AssertionError("Mixed favorites corrupted settings");
  cfg.save();var again=VideoMenuConfig.load();
  if(!again.libraryFavorites.equals(cfg.libraryFavorites)||again.fps!=60||!again.inGameMenuBackground)throw new AssertionError("Round-trip changed settings");
  Files.writeString(file,"{\"libraryFavorites\":null,\"audioVolume\":999,\"fps\":0}");
  cfg=VideoMenuConfig.load();if(!cfg.libraryFavorites.isEmpty()||cfg.audioVolume!=1.0||cfg.fps!=15)throw new AssertionError("Malformed preferences bypassed validation");
  Files.writeString(file,"{broken");cfg=VideoMenuConfig.load();if(cfg.inGameMenuBackground)throw new AssertionError("Recovery enabled in-world playback");
  System.out.println("PASS malformed favorites, deduplication, config round-trip, bounds and broken JSON fallback");
 }
}

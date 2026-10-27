package com.videomenu;
import java.util.Properties;
/** Publisher configuration only. Players never supply account tokens or application IDs. */
public final class EvatexDiscord {
    private static EvatexDiscordPresence presence;
    private EvatexDiscord(){}
    public static synchronized void configure(VideoMenuConfig config){
        if(presence==null){
            String id="";
            try(var stream=EvatexDiscord.class.getResourceAsStream("/assets/evatexbackgroundvideo/discord.properties")){
                if(stream!=null){Properties p=new Properties();p.load(stream);id=p.getProperty("applicationId","").trim();}
            }catch(Exception ignored){}
            presence=new EvatexDiscordPresence(id);
        }
        presence.setEnabled(config.discordPresence);
    }
    public static synchronized String status(){return presence==null?"evatex.discord.off":presence.status;}
    public static synchronized String diagnostics(){return presence==null?"Discord activity: inactive\n":presence.diagnostics();}
    public static synchronized void close(){if(presence!=null){presence.close();presence=null;}}
}

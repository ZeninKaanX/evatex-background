package com.videomenu;

import java.util.Properties;
import java.util.UUID;

/** Same-JVM handoff. Property critical sections never perform socket or file I/O. */
public final class DiscordActivityCoordinator {
    private static final String BASE="eververity.discord.v1.";
    private static final long ELIGIBLE_TTL=12_000_000_000L, OWNER_TTL=5_000_000_000L;
    private final String role,claim;
    private final Properties properties;
    private long lastPublication;
    private boolean published;
    public DiscordActivityCoordinator(String role){this(role,System.getProperties());}
    public DiscordActivityCoordinator(String role,Properties properties){
        if(!role.equals("volt")&&!role.equals("evatex"))throw new IllegalArgumentException("Unknown activity role");
        this.role=role;this.properties=properties;claim=role+":"+UUID.randomUUID();
    }
    public synchronized void eligible(boolean value){
        long now=System.nanoTime();
        if(value && (!published || now-lastPublication>3_000_000_000L)){
            properties.setProperty(BASE+role+".eligible",Long.toString(now));lastPublication=now;
        } else if(!value && published) properties.remove(BASE+role+".eligible");
        published=value;
    }
    private long number(String key){try{return Long.parseLong(properties.getProperty(BASE+key,"0"));}catch(NumberFormatException malformed){return 0;}}
    private boolean fresh(long stamp,long ttl,long now){return stamp!=0 && now-stamp>=0 && now-stamp<ttl;}
    public boolean acquire(){
        synchronized(properties){
            long now=System.nanoTime();
            String wanted=fresh(number("volt.eligible"),ELIGIBLE_TTL,now)?"volt":fresh(number("evatex.eligible"),ELIGIBLE_TTL,now)?"evatex":"";
            if(!role.equals(wanted))return false;
            String owner=properties.getProperty(BASE+"owner","");
            if(!owner.isEmpty()&&!owner.equals(claim)&&fresh(number("owner.at"),OWNER_TTL,now))return false;
            properties.setProperty(BASE+"owner",claim);properties.setProperty(BASE+"owner.at",Long.toString(now));return true;
        }
    }
    public boolean owns(){synchronized(properties){return claim.equals(properties.getProperty(BASE+"owner"));}}
    /** Call only after clearing and disconnecting the previous activity. */
    public void release(){synchronized(properties){if(owns()){properties.remove(BASE+"owner");properties.remove(BASE+"owner.at");}}}
}

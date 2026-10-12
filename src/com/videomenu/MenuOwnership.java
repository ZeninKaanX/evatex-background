package com.videomenu;

/** Stops two menu owners from replacing each other's screens every tick. */
public final class MenuOwnership {
    public enum Kind { NONE, VANILLA, EVATEX, FOREIGN }
    private boolean enabledBefore, entered, yielded, away;
    public boolean shouldOpen(boolean enabled, boolean worldLoaded, Kind kind) {
        if(enabled!=enabledBefore || worldLoaded){entered=false;yielded=false;away=false;enabledBefore=enabled;}
        if(!enabled || worldLoaded)return false;
        if(kind==Kind.EVATEX){entered=true;away=false;return false;}
        if(kind==Kind.NONE){if(entered)away=true;return false;}
        if(kind==Kind.VANILLA && entered && away && !yielded){away=false;return true;}
        if(kind==Kind.FOREIGN || kind==Kind.VANILLA && entered){yielded=true;return false;}
        if(kind==Kind.VANILLA && !yielded){entered=true;return true;}
        return false;
    }
}

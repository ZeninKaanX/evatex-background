import com.videomenu.MenuOwnership;
import static com.videomenu.MenuOwnership.Kind.*;
public final class MenuOwnershipRegression {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        var policy=new MenuOwnership();
        check(!policy.shouldOpen(false,false,VANILLA),"default off");
        check(policy.shouldOpen(true,false,VANILLA),"explicit enable on vanilla");
        check(!policy.shouldOpen(true,false,EVATEX),"keep own screen");
        check(!policy.shouldOpen(true,false,NONE),"options/loading/null transition");
        check(policy.shouldOpen(true,false,VANILLA),"legacy child closes to native title once");
        check(!policy.shouldOpen(true,false,EVATEX),"return to own screen");
        check(!policy.shouldOpen(true,false,VANILLA),"another mod took over");
        for(int i=0;i<1000;i++)check(!policy.shouldOpen(true,false,VANILLA),"no menu replacement loop");
        check(!policy.shouldOpen(false,false,VANILLA),"disable clears ownership");
        check(policy.shouldOpen(true,false,VANILLA),"user can enable again");
        check(!policy.shouldOpen(true,true,NONE),"world loaded releases ownership");
        check(policy.shouldOpen(true,false,VANILLA),"fresh main menu after playing");
        var foreign=new MenuOwnership();
        check(!foreign.shouldOpen(true,false,FOREIGN),"preserve custom menu from startup");
        check(!foreign.shouldOpen(true,false,VANILLA),"preserve its vanilla return route");
        System.out.println("PASS cooperative menu ownership: default off, custom menus, null/loading transitions, 1000 competing replacements, setting reset and world return");
    }
}

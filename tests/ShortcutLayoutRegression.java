import com.videomenu.ShortcutLayout;
import com.videomenu.ShortcutLayout.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ShortcutLayoutRegression {
    static void require(boolean value,String message) {if (!value) throw new AssertionError(message);}
    public static void main(String[] args) {
        var empty=List.<Rect>of();
        Rect preferred=ShortcutLayout.choose(427,240,100,14,4,false,empty,null);
        require(preferred.equals(new Rect(163,214,100,14)),"native clean anchor");
        require(ShortcutLayout.choose(427,240,100,14,4,true,empty,null).x()==323,"right anchor");
        List<Rect> vanilla=List.of(new Rect(113,192,98,20),new Rect(215,192,98,20),new Rect(335,230,88,10),new Rect(0,232,427,8));
        require(ShortcutLayout.choose(427,240,100,14,2,false,vanilla,null).equals(preferred),"native footer spacing keeps approved centred anchor");
        List<Rect> occupied=List.of(preferred);
        Rect moved=ShortcutLayout.choose(427,240,100,14,4,false,occupied,preferred);
        require(ShortcutLayout.clear(moved,427,240,occupied,4),"competing control");
        require(moved.x()==preferred.x(),"fallback stays visually centred");
        require(ShortcutLayout.choose(427,240,100,14,4,false,empty,moved).equals(preferred),"freed anchor restored");
        require(ShortcutLayout.choose(80,240,100,14,4,false,empty,null)==null,"narrow full button rejected");
        require(ShortcutLayout.clear(ShortcutLayout.choose(80,240,72,14,4,false,empty,null),80,240,empty,4),"compact fallback");
        require(ShortcutLayout.choose(427,240,100,14,4,false,List.of(new Rect(0,0,427,240)),null)==null,"full menu no overlap");
        var dense=List.of(new Rect(24,0,379,230));
        require(ShortcutLayout.choose(427,240,100,14,2,false,dense,null)==null,"dense client has no horizontal gap");
        Rect rail=ShortcutLayout.chooseSide(427,240,100,2,dense);
        require(rail!=null && rail.width()==18 && rail.height()==100 && rail.x()==407,"dense client right vertical rail");
        require(ShortcutLayout.clear(rail,427,240,dense,2),"vertical rail bounds and gap");
        require(ShortcutLayout.chooseSide(427,50,100,2,dense)==null,"short menu rejects unreadable rail");
        Random random=new Random(83271);
        for(int iteration=0;iteration<2000;iteration++) {
            int w=80+random.nextInt(1600),h=25+random.nextInt(900);
            List<Rect> controls=new ArrayList<>();
            for(int n=0;n<random.nextInt(25);n++) controls.add(new Rect(random.nextInt(w),random.nextInt(h),10+random.nextInt(240),5+random.nextInt(80)));
            Rect result=ShortcutLayout.choose(w,h,100,14,4,false,controls,null);
            require(result==null || ShortcutLayout.clear(result,w,h,controls,4),"random collisions/bounds");
            require(java.util.Objects.equals(result,ShortcutLayout.choose(w,h,100,14,4,false,controls,null)),"deterministic placement");
            if(result!=null) {
                Rect resize=ShortcutLayout.choose(w+37,h+19,100,14,4,false,controls,result);
                require(resize==null || ShortcutLayout.clear(resize,w+37,h+19,controls,4),"resize bounds");
            }
        }
        System.out.println("PASS shortcut clean anchors, collision relocation, restoration, compact/full menus, vertical side rails, 2000 random layouts and resize");
    }
}

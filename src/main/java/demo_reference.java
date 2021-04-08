import com.sun.corba.se.spi.ior.ObjectKey;

import java.lang.ref.SoftReference;

/**
 * @author xitianyu
 * @description 软引用，内存不足时避免OOM，回收对象
 * @date 2021/3/22
 */
public class demo_reference {

    public static void main(String[] args) {

        System.out.println("start");
        Obj obj = new Obj();
        obj.obj[0] = 1;
        obj.obj[1] = 2;
        SoftReference<Obj> sr = new SoftReference<>(obj);
        obj = null;
        Obj obj1 = sr.get();
        System.out.println("sr.get() = " + sr.get().obj[0]);
        System.out.println("sr.get() = " + sr.get().obj[1]);
        System.out.println("end");
    }
}
class Obj {
    int[] obj;
    public Obj(){
        obj = new int[1000];
    }
}
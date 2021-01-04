import java.io.*;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/2
 */
public class demo_serializable implements Externalizable{

    private transient String content = "是的，我将会被序列化，不管我是否被transient关键字修饰";

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(content);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        content = (String) in.readObject();
    }

    public static void main(String[] args) throws Exception {

        demo_serializable et = new demo_serializable();
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(
                new File("test")));
        out.writeObject(et);

        ObjectInput in = new ObjectInputStream(new FileInputStream(new File(
                "test")));
        et = (demo_serializable) in.readObject();
        System.out.println(et.content);

        out.close();
        in.close();
    }

}

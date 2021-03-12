import java.io.*;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/2
 */
public class demo_serializable implements Serializable {

    private transient String content = "23343234";

    private String data = "123123";

    public String getData() {

        return data;
    }

    public String getContent() {

        return content;
    }
    //    @Override
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeObject(content);
//    }
//
//    @Override
//    public void readExternal(ObjectInput in) throws IOException,
//            ClassNotFoundException {
//        content = (String) in.readObject();
//    }

    public static void main(String[] args) throws Exception {

        demo_serializable et = new demo_serializable();
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(
                new File("test")));
        out.writeObject(et);

        ObjectInput in = new ObjectInputStream(new FileInputStream(new File(
                "test")));
        demo_serializable res = new demo_serializable();
        res = (demo_serializable) in.readObject();
        System.out.println(res.getData());
        System.out.println(res.getContent());

        out.close();
        in.close();
    }

}

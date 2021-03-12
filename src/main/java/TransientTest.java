/**
 * @author xitianyu
 * @description
 * @date 2020/12/2
 */
import java.io.*;

/**
 * @description 使用transient关键字不序列化某个变量
 *        注意读取的时候，读取数据的顺序一定要和存放数据的顺序保持一致
 *
 * @author Alexia
 * @date  2013-10-15
 */
public class TransientTest {

    public static void main(String[] args) {

        Person Person = new Person();
        Person.setUsername("xxxaaa4");
        Person.setPasswd("123456");

        System.out.println("read before Serializable: ");
        System.out.println("Username: " + Person.getUsername());
        System.err.println("password: " + Person.getPasswd());

        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream("C:\\Users\\xitianyu\\Desktop\\data\\person.txt"));
            os.writeObject(Person); // 将Person对象写进文件
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // 在反序列化之前改变Personname的值
            Person.setUsername("xty");

            ObjectInputStream is = new ObjectInputStream(new FileInputStream(
                    "C:\\Users\\xitianyu\\Desktop\\data\\person.txt"));
            Person = (Person) is.readObject(); // 从流中读取Person的数据
            is.close();

            System.out.println("\nread after Serializable: ");
            System.out.println("Username: " + Person.getUsername());
            System.err.println("password: " + Person.getPasswd());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class Person implements Serializable {
    private static final long serialVersionUID = 8294180014912103005L;

    public static String username;
    private transient String passwd;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

}

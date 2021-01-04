/**
 * @author xitianyu
 * @description
 * @date 2020/12/2
 */
import java.io.*;

/**
 * @description ʹ��transient�ؼ��ֲ����л�ĳ������
 *        ע���ȡ��ʱ�򣬶�ȡ���ݵ�˳��һ��Ҫ�ʹ�����ݵ�˳�򱣳�һ��
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
            os.writeObject(Person); // ��Person����д���ļ�
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // �ڷ����л�֮ǰ�ı�Personname��ֵ
            Person.setUsername("xty");

            ObjectInputStream is = new ObjectInputStream(new FileInputStream(
                    "C:\\Users\\xitianyu\\Desktop\\data\\person.txt"));
            Person = (Person) is.readObject(); // �����ж�ȡPerson������
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

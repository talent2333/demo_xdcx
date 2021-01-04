import java.io.UnsupportedEncodingException;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/3
 */
public class demo_encode {

    public static void main(String[] args) throws UnsupportedEncodingException {

        byte[] b_gbk = "жа".getBytes("GBK");
        byte[] b_utf8 = "жа".getBytes("UTF-8");
        byte[] b_iso88591 = "жа".getBytes("ISO8859-1");
        System.out.println(b_utf8.length);//3
        System.out.println(b_gbk.length); //2
        System.out.println(b_iso88591.length);//1
        String s = new String(b_utf8, "UTF-8");
        System.out.println("s = " + s);

    }

}

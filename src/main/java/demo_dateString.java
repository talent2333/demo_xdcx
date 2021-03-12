import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/3
 */
public class demo_dateString {

    public static LocalDateTime eval(String timeStamp, String format) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = sdf.parse(timeStamp);
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));

        LocalDateTime stringToLocalDateTime =
                LocalDateTime.parse(sdf1.format(date), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        return stringToLocalDateTime;
    }

    public static void main(String[] args) throws ParseException {

        String date = "2020-12-03 09:30:00";
        Date dateResult = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date1 = sdf2.parse(date);

        System.out.println("dateResult = " + dateResult);
        System.out.println("date1 = " + date1);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        String result = dateTimeFormatter.format(now);
        System.out.println("result = " + result);

        String result2 = now.format(DateTimeFormatter.ofPattern("HH:mm:ss.S"));
        System.out.println("result2 = " + result2);

    }

    @Test
    public void test001() throws ParseException {
        String str = "1970-01-01 00:00:01";
        Timestamp timestamp = Timestamp.valueOf(str);
        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        System.out.println("localDateTime = " + localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

    }

}
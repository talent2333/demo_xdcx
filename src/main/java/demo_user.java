import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xitianyu
 * @description
 * @date 2020/11/30
 */
public class demo_user {

    public static void main(String[] args) {

        ArrayList<User> users = new ArrayList<>();
        users.add(new User("xiaohong",20,"female","123456"));
        users.add(new User("xiaoming",20,"male","888888"));
        users.add(new User("xiaoxi",20,"male","000321"));

        String str = new String("11:22:33");
        String[] split = str.split(":");
        List<String> phones = users.stream().map(User::getPhoneNumber).collect(Collectors.toList());
        System.out.println("phones = " + phones);

    }

}

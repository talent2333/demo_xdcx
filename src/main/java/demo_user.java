import com.google.common.collect.Lists;

import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xitianyu
 * @description Collectors Test
 * @date 2021/02/02
 */
public class demo_user {

    public static void main(String[] args) {

        ArrayList<User> users = Lists.newArrayList();
        users.add(new User("jack",20,"female","123456",500));
        users.add(new User("marry",20,"female","888888",300));
        users.add(new User("xx",20,"male","010101",2000));
        users.add(new User("yy",20,"male","000111",1000));
        users.add(new User("zz",20,"male","111000",2000));

        Double total = users.stream().collect(Collectors.summingDouble(User::getSalary));
        System.out.println("total = " + total);

        String joined = users.stream().map(Object::toString).collect(Collectors.joining("->"));
        System.out.println("joined = " + joined);

        Map<String, List<User>> byGender = users.stream().collect(Collectors.groupingBy(User::getGender));
        byGender.entrySet().stream().forEach(t-> System.out.printf("%s:%s\n",t.getKey(),t.getValue()));

        Map<String, Double> byGenderSummingSalary = users.stream().collect(Collectors.groupingBy(User::getGender, Collectors.summingDouble(User::getSalary)));
        byGenderSummingSalary.entrySet().stream().forEach(t-> System.out.printf("%s:%s\n",t.getKey(),t.getValue()));
    }

}

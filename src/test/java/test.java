import org.junit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/1
 */
public class test {

    @Test
    public void test001(){

        ArrayList<String> forEachLists = new ArrayList<>();
        forEachLists.add("a");
        forEachLists.add("b");
        forEachLists.add("c");
        forEachLists.add("d");
        forEachLists.add("e");
        forEachLists.add("f");
        forEachLists.stream().skip(3).limit(3).forEach(System.out::println);
    }

    @Test
    public void test002(){

        List<Integer> sortLists = new ArrayList<>();
        sortLists.add(1);
        sortLists.add(4);
        sortLists.add(6);
        sortLists.add(3);
        sortLists.add(2);
        List<Integer> collect = sortLists.stream().sorted((t1,t2)->t1-t2).collect(Collectors.toList());
        System.out.println("collect = " + collect);
    }

    @Test
    public void test003(){

        List<String> sortLists = new ArrayList<>();
        sortLists.add("1");
        sortLists.add("4");
        sortLists.add("6");
        sortLists.add("3");
        sortLists.add("2");
        boolean res = sortLists.stream().anyMatch(s -> s.contains("9"));
        System.out.println("res = " + res);
    }
    @Test
    public void test004(){

        List<String> sortLists = new ArrayList<>();
        sortLists.add("13");
        sortLists.add("44");
        sortLists.add("66");
        sortLists.add("12");
        sortLists.add("1");
        Stream<String> stringStream = sortLists.stream().filter(s -> s.length() != 1 && s.startsWith("1"));
        List<String> collect = stringStream.collect(Collectors.toList());
        stringStream.forEach(t->System.out.printf("%s ",t));
    }
}

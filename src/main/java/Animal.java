import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Animal {

    private String name;
    private String address;
    private Integer age;
    private String phone;

}

class AnimalTest{

    public static void main(String[] args) {

        Animal animal = new Animal();
        animal.setName("dog");
        String name = animal.getName();
        System.out.println("name = " + name);
    }
}
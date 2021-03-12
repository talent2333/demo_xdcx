import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xitianyu
 * @description
 * @date 2020/11/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String name;
    private Integer age;
    private String gender;
    private String phoneNumber;
    private double salary;

}

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;
import org.apache.flink.types.Row;

import java.time.Instant;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/30
 */
public class demo_udf extends ScalarFunction {

    @DataTypeHint("ROW<s String,TIMESTAMP(3) WITH LOCAL TIME ZONE")
    public Row eval(int i){

        return Row.of(String.valueOf(i), Instant.ofEpochSecond(i));
    }

}

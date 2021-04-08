import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * @author xitianyu
 * @description 查询json字符串中第二层的数组中的元素
 * @date 2021/3/25
 */
public class GetMulJsonField extends ScalarFunction {

    public String eval(String json, String field1,String field2) {

        if (field1 == null || field1.trim().equals("")) {
            return "";
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final JsonNode root = objectMapper.readTree(json);
            JsonNode jsonNode1 = root.findValue(field1.trim());
            String str1 = jsonNode1.toString();
            //去除json第一层字符串中的 【 和 】
            String substr = str1.substring(1, str1.length() - 1);
            JsonNode root2 = objectMapper.readTree(substr);
            JsonNode jsonNode2 = root2.findValue(field2.trim());

            String result = jsonNode2.asText();
            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid Json.");
        }
    }

    public static void main(String[] args) {

        String source = "{\"couponAmount\":565," +
                "\"couponDetailList\":[{\"accountType\":2,\"batchId\":0,\"couponAmount\":565," +
                "\"couponForm\":9,\"couponId\":1,\"couponTitle\":\"满1单打9.5折\",\"couponType\":\"2\"," +
                "\"couponTypeText\":\"小专车折扣\",\"couponValue\":\"95\",\"discount\":\"95\"}]}";

        GetMulJsonField getMulJsonField = new GetMulJsonField();
        String couponForm = getMulJsonField.eval(source, "couponDetailList","couponForm");
        System.out.println("couponForm = " + couponForm);
    }
}

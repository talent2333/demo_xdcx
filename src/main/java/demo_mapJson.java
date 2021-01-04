import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.type.MapType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/3
 */
public class demo_mapJson {

    private static HashMap<String, String> map = new HashMap<>();

    @Before
    public void before(){
        map.put("name","xty");
        map.put("age","26");
        map.put("hobby","eat");
        map.put("birthplace","shanghai");
    }
    @Test
    public  void testMapToJson() throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(map);
        System.out.println("json = " + json);
    }
    @Test
    public  void testJsonToMap() throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(map);
        System.out.println("json = " + json);
        ObjectMapper objectMapper = new ObjectMapper();
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        HashMap<String,String> o = (HashMap<String,String>)objectMapper.readValue(json, mapType);
//        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, String> next = iterator.next();
//            System.out.println("key: "+next.getKey()+",value: "+next.getValue());
//        }
        o.entrySet().stream().forEach(t->System.out.printf("%s ",t));
    }

    @Test
    public void testMapTree() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper();
        String jsonString = "{\"name\":\"Mahesh Kumar\", \"age\":21,\"verified\":false,\"marks\": {\"mark1\":\"100\",\"mark2\":\"200\"}}}";
        JsonNode jsonNode = om.readTree(jsonString);
        JsonNode markNode = null;
        Iterator<String> iter = jsonNode.fieldNames();
        JsonNode marks = jsonNode.path("marks");
        JsonNode mark1 = marks.path("mark2");
        System.out.println(mark1.asText());

    }

}

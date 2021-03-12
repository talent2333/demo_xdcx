import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.type.MapType;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author xitianyu
 * @description
 * @date 2020/12/3
 */
public class demo_mapJson {

    private static HashMap<String, String> map = new HashMap<>();
//  private static LinkedHashMap<String,String> map = new LinkedHashMap<>();

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
    public void testMap() throws IOException {

        ObjectMapper om = new ObjectMapper();
        HashMap<String,String> data = new HashMap<>();
        File file = new File("t1");

        HashMap<String,String> map = om.readValue(file, HashMap.class);

        map.entrySet().stream().forEach(t-> System.out.printf("%s:%s ",t.getKey(),t.getValue()));

    }
    @Test
    public  void testJsonToMap() throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(map);
        System.out.println("json = " + json);
        ObjectMapper om = new ObjectMapper();
        MapType mapType = om.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        HashMap<String,String> data = (HashMap<String,String>)om.readValue(json, mapType);
//        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, String> next = iterator.next();
//            System.out.println("key: "+next.getKey()+",value: "+next.getValue());
//        }
        data.entrySet().stream().forEach(t->System.out.printf("%s:%s ",t.getKey(),t.getValue()));
    }

    @Test
    public void testMapTree() throws JsonProcessingException {

        ObjectMapper om = new ObjectMapper();
        String jsonString = "{\"name\":\"Mahesh Kumar\", \"age\":21,\"verified\":false,\"marks\": {\"mark1\":\"150\",\"mark2\":\"220\"}}}";
        JsonNode jsonNode = om.readTree(jsonString);
        JsonNode markNode = null;
        Iterator<String> iter = jsonNode.fieldNames();
        JsonNode marks = jsonNode.path("marks");
        JsonNode mark1 = marks.path("mark2");
        System.out.println(mark1.asText());

    }

}

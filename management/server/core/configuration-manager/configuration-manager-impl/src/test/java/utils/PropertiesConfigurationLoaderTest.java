package utils;


import com.google.gson.*;
import org.junit.Test;

import java.util.Map;
import java.util.Set;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoaderTest {

	//    @Test
	//    public void convertStringToJson() {
	//        System.out.println( "convert" );
	//    }
	//


	@Test
	public void test() {
//        PropertiesConfigurationLoader loader = new PropertiesConfigurationLoader();
		//        Config o = loader.getConfiguration( null, null, null );
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//        System.out.println( json );


		JsonObject jo = new JsonObject();
		jo.addProperty("path", "full/path/to/file");
		jo.addProperty("type", "YAML");

		JsonObject cf = new JsonObject();
		cf.addProperty("fieldName", "field_name");
		cf.addProperty("label", "Label");
		cf.addProperty("required", true);
		cf.addProperty("uiType", "TextField");
		cf.addProperty("value", "Value");

		JsonArray jsonArray = new JsonArray();
		jsonArray.add(cf);
		jo.add("configFields", jsonArray);

		Gson gsonNew = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println( gson.toJson( jo ) );

		Set<Map.Entry<String, JsonElement>> set = jo.entrySet();
		for (Map.Entry<String, JsonElement> stringJsonElementEntry : set) {
//            System.out.println( stringJsonElementEntry.getKey() + " " + stringJsonElementEntry.getValue() );
		}

		JsonArray jsonArray2 = jo.getAsJsonArray("configFields");
//        System.out.println( jsonArray2.size() );
//        System.out.println( "test" );
		for (int i = 0; i < jsonArray2.size(); i++) {
			JsonObject jo1 = (JsonObject) jsonArray2.get(i);
			String fieldName = jo1.getAsJsonPrimitive("fieldName").getAsString();
			String value = jo1.getAsJsonPrimitive("value").getAsString();
//            System.out.println( fieldName + " " + value );
		}
	}
}

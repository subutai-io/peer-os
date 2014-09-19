package utils;


import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoaderTest {


    @Test
    public void test() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        JsonObject jo = new JsonObject();
        jo.addProperty( "path", "full/path/to/file" );
        jo.addProperty( "type", "YAML" );

        JsonObject cf = new JsonObject();
        cf.addProperty( "fieldName", "field_name" );
        cf.addProperty( "label", "Label" );
        cf.addProperty( "required", true );
        cf.addProperty( "uiType", "TextField" );
        cf.addProperty( "value", "Value" );

        JsonArray jsonArray = new JsonArray();
        jsonArray.add( cf );
        jo.add( "configFields", jsonArray );

        Gson gsonNew = new GsonBuilder().setPrettyPrinting().create();

        Set<Map.Entry<String, JsonElement>> set = jo.entrySet();
        for ( Map.Entry<String, JsonElement> stringJsonElementEntry : set )
        {
        }

        JsonArray jsonArray2 = jo.getAsJsonArray( "configFields" );
        for ( int i = 0; i < jsonArray2.size(); i++ )
        {
            JsonObject jo1 = ( JsonObject ) jsonArray2.get( i );
            String fieldName = jo1.getAsJsonPrimitive( "fieldName" ).getAsString();
            String value = jo1.getAsJsonPrimitive( "value" ).getAsString();
        }
    }
}

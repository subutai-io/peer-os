package utils;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/18/14.
 */
public class ConfigBuilderTest {


    @Test
    public void test() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        JsonObject jo = configBuilder.getConfigJsonObject("test", ConfigTypeEnum.PROPERTIES);

        JsonObject field1 = configBuilder.buildFieldJsonObject( "field name", "Field Name", true, "textarea", true, "value" );

        List<JsonObject> fields = new ArrayList<>();
        fields.add( field1 );

        JsonObject njo = configBuilder.addJsonArrayToConfig( jo, fields );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

//        System.out.println(gson.toJson( njo ));

    }

}

package utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigBuilder;
import org.safehaus.subutai.configuration.manager.impl.utils.FileContentReader;
import org.safehaus.subutai.configuration.manager.impl.utils.IniParser;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/19/14.
 */
public class FileContentReaderTest {

    @Test
    public void test() {
        FileContentReader fileContentReader = new FileContentReader();
        String filePath =
                "/home/bahadyr/SUBUTAI/main/management/server/core/configuration-manager/configuration-manager-impl"
                        + "/src/main/resources/flume_conf/flume-conf.properties";
        String content = fileContentReader.readFile( filePath );
        //        System.out.println( content );


        try {
            IniParser iniParser = null;
            iniParser = new IniParser( content );
            PropertiesConfiguration propertiesConfiguration = iniParser.getConfig();
            Iterator<String> iterator = propertiesConfiguration.getKeys();

            ConfigBuilder configBuilder = new ConfigBuilder();
            JsonObject jo = configBuilder.getConfigJsonObject( filePath, ConfigTypeEnum.PROPERTIES );
            List<JsonObject> fields = new ArrayList<>();
            while ( iterator.hasNext() ) {
                String key = iterator.next();
                String value = iniParser.getStringProperty( key );
                JsonObject field = configBuilder.buildFieldJsonObject( key, "", "", "", value );
                fields.add( field );
            }
            JsonObject cjo = configBuilder.addJsonArrayToConfig( jo, fields );
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson( cjo ));
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
    }
}

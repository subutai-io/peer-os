package utils;


import org.junit.Test;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;
import org.safehaus.subutai.configuration.manager.impl.utils.ConfigParser;
import org.safehaus.subutai.configuration.manager.impl.utils.FileContentReader;
import org.safehaus.subutai.configuration.manager.impl.utils.IniParser;
import org.safehaus.subutai.shared.protocol.FileUtil;

import org.apache.commons.configuration.ConfigurationException;

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
        //        String filePath =
        //                "/home/bahadyr/SUBUTAI/main/management/server/core/configuration-manager/configuration
        // -manager-impl"
        //                        + "/src/main/resources/flume_conf/flume-conf.properties";

        String filePath = "cassandra_conf/cassandra.yaml";
        String content = FileUtil.getContent( filePath, this );

//        System.out.println(content);
       /* try {
            ConfigParser iniParser = new IniParser( content );
            JsonObject cjo = iniParser.parserConfig( filePath, ConfigTypeEnum.PROPERTIES );
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println( gson.toJson( cjo ) );
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }*/
    }
}

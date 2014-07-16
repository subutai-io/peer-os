package utils;


import org.junit.Test;
import org.safehaus.subutai.configuration.manager.impl.utils.PropertiesConfigurationLoader;


/**
 * Created by bahadyr on 7/16/14.
 */
public class PropertiesConfigurationLoaderTest {

    @Test
    public void test() {
        PropertiesConfigurationLoader loader = new PropertiesConfigurationLoader();
        Object o = loader.loadConfig( null );
    }

}

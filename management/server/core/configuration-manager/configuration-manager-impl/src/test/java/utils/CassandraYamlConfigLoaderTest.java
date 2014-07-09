package utils;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.configuration.manager.impl.utils.CassandraYamlLoader;

import org.apache.cassandra.config.Config;


/**
 * CassandraYamlConfigLoader Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Jul 9, 2014</pre>
 */
public class CassandraYamlConfigLoaderTest {

    @Before
    public void before() throws Exception {
    }


    @After
    public void after() throws Exception {
    }


    /**
     * Method: loadConfiguration()
     */
    @Test
    public void testLoadConfiguration() throws Exception {
        //TODO: Test goes here...
        System.out.println( "TEST LOAD CONFIGURATION" );

        CassandraYamlLoader c = new CassandraYamlLoader();

        Config o = c.loadConfig();
        for ( String d : o.data_file_directories ) {
            System.out.println( d );
        }
    }
} 

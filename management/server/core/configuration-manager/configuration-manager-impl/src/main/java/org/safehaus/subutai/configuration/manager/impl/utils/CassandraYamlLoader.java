package org.safehaus.subutai.configuration.manager.impl.utils;


import java.beans.IntrospectionException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.MissingProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.SeedProviderDef;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.io.util.FileUtils;


/**
 * Created by bahadyr on 7/9/14.
 */
public class CassandraYamlLoader extends YamlConfigurationLoader {


    public CassandraYamlLoader() {
        System.setProperty( "cassandra.config", "cassandra.2.0.5/cassandra.yaml" );
    }


    public Config loadConfig( InputStream input ) throws ConfigurationException {
        //        InputStream input = null;
        try {
            //            URL url = getStorageConfigURL();
            //            try {
            //                input = url.openStream();
            //            }
            //            catch ( IOException e ) {
            //                getStorageConfigURL should have ruled this out
            //                throw new AssertionError( e );
            //            }
            org.yaml.snakeyaml.constructor.Constructor constructor =
                    new org.yaml.snakeyaml.constructor.Constructor( Config.class );
            TypeDescription seedDesc = new TypeDescription( SeedProviderDef.class );
            seedDesc.putMapPropertyType( "parameters", String.class, String.class );
            constructor.addTypeDescription( seedDesc );
            MissingPropertiesChecker propertiesChecker = new MissingPropertiesChecker();
            constructor.setPropertyUtils( propertiesChecker );
            Yaml yaml = new Yaml( constructor );
            Config result = yaml.loadAs( input, Config.class );
            propertiesChecker.check();
            return result;
        }
        catch ( YAMLException e ) {
            throw new ConfigurationException( "Invalid yaml", e );
        }
        finally {
            FileUtils.closeQuietly( input );
        }
    }


    private static class MissingPropertiesChecker extends PropertyUtils {
        private final Set<String> missingProperties = new HashSet<>();


        public MissingPropertiesChecker() {
            setSkipMissingProperties( true );
        }


        @Override
        public Property getProperty( Class<? extends Object> type, String name ) throws IntrospectionException {
            Property result = super.getProperty( type, name );
            if ( result instanceof MissingProperty ) {
                missingProperties.add( result.getName() );
            }
            return result;
        }


        public void check() throws ConfigurationException {
            if ( !missingProperties.isEmpty() ) {
                throw new ConfigurationException(
                        "Invalid yaml. Please remove properties " + missingProperties + " from your cassandra.yaml" );
            }
        }
    }
}

package utils;


import org.junit.Test;
import org.safehaus.subutai.common.util.FileUtil;
import org.yaml.snakeyaml.Yaml;

import java.util.Iterator;
import java.util.Map;


/**
 * Created by bahadyr on 7/15/14.
 */
public class YamlConfigurationLoaderTest {

	//    String filePath =
	//            "/home/bahadyr/SUBUTAI/main/management/server/core/configuration-manager/configuration/src/main
	// /resources" +
	//                    "/cassandra_conf/cassandra.yaml";


	@Test
	public void test() {


		String filePath = "cassandra_conf/cassandra.yaml";
		String content = FileUtil.getContent(filePath, this);

		Yaml yaml = new Yaml();
		Map<String, Object> config = (Map<String, Object>) yaml.load(content);
		Iterator iterator = config.entrySet().iterator();
		for (String key : config.keySet()) {

			Object value = config.get(key);
//            System.out.println( key + " " + value);
		}
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author bahadyr
 */
public class CassandraNameChangeTest {

    public CassandraNameChangeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
//    @Test
    public void hello() {
        String changeNameCommand = "sed -i \"$(sed -n '/cluster_name:/=' /opt/cassandra-2.0.0/conf/cassandra.yaml)\"'s/Test Cluster/'\"%name\"'/' /opt/cassandra-2.0.0/conf/cassandra.yaml";
        changeNameCommand = changeNameCommand.replace("%name", "myname");
        try {
            System.out.println(changeNameCommand);
            Process p = Runtime.getRuntime().exec(changeNameCommand);
//            System.out.println(p.exitValue());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

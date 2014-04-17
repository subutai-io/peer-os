/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.dbmanager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author dilshat
 * @todo use classpath sql file with one single table
 */
//@Ignore
public class DbManagerImplTest {

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("pi.sql", true, true, "test"));
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final DbManager dbManager = new DbManagerImpl();

    private final String source = "source";
    private final String key = "key";
    private final String content = "content";

    public DbManagerImplTest() {

    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        ((DbManagerImpl) dbManager).setTestSession(cassandraCQLUnit.session);
    }

    @After
    public void tearDown() {
        dbManager.deleteInfo(source, key);
    }

    private static class MyPojo {

        private String content;

        public MyPojo(String test) {
            this.content = test;
        }

        public String getTest() {
            return content;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyPojo other = (MyPojo) obj;
            if ((this.content == null) ? (other.content != null) : !this.content.equals(other.content)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "MyPojo{" + "content=" + content + '}';
        }

    }

    @Test
    public void testSaveInfo() {
        assertTrue(dbManager.saveInfo(source, key, new MyPojo(content)));
    }

    @Test
    public void testGetInfo() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        MyPojo myPojo2 = dbManager.getInfo(source, key, MyPojo.class);

        assertEquals(myPojo, myPojo2);
    }

    @Test
    public void testGetInfoList() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        List<MyPojo> list = dbManager.getInfo(source, MyPojo.class);

        assertFalse(list.isEmpty());
    }

    @Test
    public void testDeleteInfo() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        dbManager.deleteInfo(source, key);

        List<MyPojo> list = dbManager.getInfo(source, MyPojo.class);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testExecuteUpdate() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        MyPojo myPojo2 = new MyPojo("test");

        dbManager.executeUpdate("update product_info set info = ? where source = ? and key = ?", gson.toJson(myPojo2), source, key);

        MyPojo myPojo3 = dbManager.getInfo(source, key, MyPojo.class);

        assertEquals(myPojo2.content, myPojo3.content);
    }

    @Test
    public void testExecuteQuery() {
        MyPojo myPojo = new MyPojo(content);

        dbManager.saveInfo(source, key, myPojo);

        ResultSet rs = dbManager.executeQuery("select * from product_info where source = ? and key = ?", source, key);

        Row row = rs.one();

        assertNotNull(row);
    }

}

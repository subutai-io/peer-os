package org.safehaus.subutai.plugin.hadoop.cli;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DescribeClusterCommandTest
{
    private DescribeClusterCommand describeClusterCommand;
    @Mock
    Hadoop hadoop;
    
    @Before
    public void setUp() throws Exception
    {
        describeClusterCommand = new DescribeClusterCommand();
        describeClusterCommand.setHadoopManager(hadoop);
    }

    @Test
    public void testGetHadoopManager() throws Exception
    {
        describeClusterCommand.getHadoopManager();

        // assertions
        assertNotNull(describeClusterCommand.getHadoopManager());
        assertEquals(hadoop, describeClusterCommand.getHadoopManager());

    }

    @Test
    public void testSetHadoopManager() throws Exception
    {
        describeClusterCommand.setHadoopManager(hadoop);

        // assertions
        assertNotNull(describeClusterCommand.getHadoopManager());

    }

    @Test
    public void testDoExecute() throws Exception
    {
        describeClusterCommand.doExecute();
    }
}
package org.safehaus.subutai.core.env.impl.builder;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.safehaus.subutai.core.env.impl.exception.NodeGroupBuildException;

import com.google.common.collect.Sets;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class NodeGroupBuildResultTest
{

    @Mock
    EnvironmentContainerImpl environmentContainer;
    @Mock
    NodeGroupBuildException exception;

    NodeGroupBuildResult result;


    @Before
    public void setUp() throws Exception
    {

        result = new NodeGroupBuildResult( Sets.newHashSet( environmentContainer ), exception );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( Sets.newHashSet( environmentContainer ), result.getContainers() );
        assertEquals( exception, result.getException() );
    }
}

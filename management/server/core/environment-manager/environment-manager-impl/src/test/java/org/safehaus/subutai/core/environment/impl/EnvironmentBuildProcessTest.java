package org.safehaus.subutai.core.environment.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.protocol.CloneContainersMessage;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;
import org.safehaus.subutai.core.environment.api.helper.ProcessStatusEnum;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class EnvironmentBuildProcessTest
{
    private static final String NAME = "name";
    EnvironmentBuildProcess process;


    @Before
    public void setUp() throws Exception
    {
        UUID blueprintUUID = UUID.randomUUID();
        process = new EnvironmentBuildProcess( blueprintUUID );
    }


    @Test
    public void testSetMessageMap() throws Exception
    {
        Map<String, CloneContainersMessage> map = new HashMap<>();
        process.setMessageMap( map );
    }


    @Test
    public void testSetTimestamp() throws Exception
    {
        long t = System.currentTimeMillis();
        process.setTimestamp( t );
        assertEquals( t, process.getTimestamp() );
    }


    @Test
    public void testUuid() throws Exception
    {
        UUID uuid = UUIDUtil.generateTimeBasedUUID();
        process.setId( uuid );
        assertEquals( uuid, process.getId() );
    }


    @Test
    public void testSetGetProcessStatusEnum() throws Exception
    {
        process.setProcessStatusEnum( ProcessStatusEnum.NEW_PROCESS );
        assertEquals( ProcessStatusEnum.NEW_PROCESS, process.getProcessStatusEnum() );
    }
}


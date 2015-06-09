package org.safehaus.subutai.core.identity.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class ShiroLoginModuleTest
{
    private ShiroLoginModule shiroLoginModule;



    @Before
    public void setUp() throws Exception
    {
        shiroLoginModule = new ShiroLoginModule();
    }


    @Test
    public void testInitialize() throws Exception
    {
    }


    @Test
    public void testLogin() throws Exception
    {

    }


    @Test
    public void testCommit() throws Exception
    {
        shiroLoginModule.commit();
    }


    @Test
    public void testAbort() throws Exception
    {
        shiroLoginModule.abort();
    }


    @Test
    public void testLogout() throws Exception
    {
    }


    @Test
    public void testGetEncryptedPassword() throws Exception
    {
    }


    @Test
    public void testCheckPassword() throws Exception
    {
    }
}
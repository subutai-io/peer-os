package io.subutai.core.key.impl;


import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.subutai.core.key.impl.KeyInfoImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class KeyInfoImplTest
{
    private static final String REAL_NAME = "real name";
    private static final String EMAIL = "email";
    private static final String PUB_KEY_ID = "pub key id";
    private static final String SUB_KEY_ID = "sub key id";

    KeyInfoImpl keyInfo;


    @Before
    public void setUp() throws Exception
    {
        keyInfo = new KeyInfoImpl( REAL_NAME, EMAIL, PUB_KEY_ID, Sets.newHashSet( SUB_KEY_ID ) );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( REAL_NAME, keyInfo.getRealName() );
        assertEquals( EMAIL, keyInfo.getEmail() );
        assertEquals( PUB_KEY_ID, keyInfo.getPublicKeyId() );
        assertTrue( keyInfo.getSubKeyIds().contains( SUB_KEY_ID ) );
    }
}

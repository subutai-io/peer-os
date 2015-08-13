package io.subutai.core.keyserver.impl.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 *
 */
@RunWith( MockitoJUnitRunner.class)
public class SecurityKeyEntityTest
{
    private SecurityKeyEntity securityKeyEntity;

    private String fingerprint = "1EB4A4CCADF438434450BF1F364CD558014A08B4";
    private String keyId       = "364CD558014A08B4";
    private String shortKeyId  = "014A08B4";
    private short  keyStatus   = 1;
    private short  keyType     = 1;

    @Before
    public void setUp() throws Exception
    {
        securityKeyEntity = new SecurityKeyEntity();

        securityKeyEntity.setFingerprint(fingerprint );
        securityKeyEntity.setKeyId( keyId );
        securityKeyEntity.setShortKeyId( shortKeyId );
        securityKeyEntity.setKeyStatus(keyStatus  );
        securityKeyEntity.setKeyType(keyType  );
    }

    @Test
    public void testFingerprint()
    {
        Assert.assertNotNull( securityKeyEntity.getFingerprint()  );
        Assert.assertEquals( fingerprint, securityKeyEntity.getFingerprint() );
    }

    @Test
    public void testKeyId()
    {
        Assert.assertNotNull( securityKeyEntity.getKeyId()  );
        Assert.assertEquals( keyId, securityKeyEntity.getKeyId() );
    }

    @Test
    public void testShortKeyId()
    {
        Assert.assertNotNull( securityKeyEntity.getShortKeyId()  );
        Assert.assertEquals( shortKeyId, securityKeyEntity.getShortKeyId() );
    }

    @Test
    public void testKeyStatus()
    {
        Assert.assertNotNull( securityKeyEntity.getKeyStatus()  );
        Assert.assertEquals( keyStatus, securityKeyEntity.getKeyStatus() );
    }

    @Test
    public void testKeyType()
    {
        Assert.assertNotNull( securityKeyEntity.getKeyType()  );
        Assert.assertEquals( keyType, securityKeyEntity.getKeyType() );
    }

}

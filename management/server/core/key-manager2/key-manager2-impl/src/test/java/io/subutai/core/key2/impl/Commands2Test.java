package io.subutai.core.key2.impl;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 *
 * Since KeyManager2Impl is using Commands2 directly, we only need to test
 * Commands2 methods.
 *
 *
 */
public class Commands2Test {

    private String EMAIL_FOR_TEST = "test@email.com";
    private String NAME_FOR_TEST = "test_name";
    private String KEY_ID_FOR_TEST = "key id for the test";
    private String KEY_ID_TO_SIGNED = "key id to be signed";
    private String SUBKEY_ID_FOR_TEST = "subkey id for test";
    private String FILEPATH_FOR_TEST = "/path/to/file/to/sign";

    @Test
    public void testGenerateTestCommand() throws Exception {
        assertNotNull ( Commands2.generateKeyCommand( NAME_FOR_TEST, EMAIL_FOR_TEST ) );
    }

    @Test
    public void testgenerateCertificateCommand() throws Exception {
        assertNotNull( Commands2.generateCertificateCommand( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testgenerateSubKey() throws Exception {
        assertNotNull( Commands2.generateSubKey( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testgetCertificate() throws Exception {
        assertNotNull( Commands2.getCertificate( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testlistAllKeys() throws Exception {
        assertNotNull( Commands2.listAllKeys() );
    }

    @Test
    public void testlistKeyWithID() throws Exception {
        assertNotNull( Commands2.listKeyWithID( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testreadKeyWithId() throws Exception {
        assertNotNull( Commands2.readKeyWithId( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testreadKeySshKeyWithId() throws Exception {
        assertNotNull( Commands2.readKeySshKeyWithId( KEY_ID_FOR_TEST ) );
    }

    @Test
    public void testsignFile() throws Exception {
        assertNotNull( Commands2.signFile( KEY_ID_FOR_TEST, FILEPATH_FOR_TEST ) );
    }

    @Test
    public void testsignKeyWithKey() throws Exception {
        assertNotNull( Commands2.signKeyWithKey( KEY_ID_FOR_TEST, KEY_ID_TO_SIGNED ));
    }

    @Test
    public void testsignKeyWithKey2() throws Exception {
        assertNotNull( Commands2.signKeyWithKey2( KEY_ID_FOR_TEST, KEY_ID_TO_SIGNED ) );
    }

    @Test
    public void testsendKeyToPublicServer() throws Exception {
        assertNotNull( Commands2.sendKeyToPublicServer( KEY_ID_FOR_TEST ) );

    }

    @Test
    public void testgenerateRevocationKey() throws Exception {
        assertNotNull( Commands2.generateRevocationKey( KEY_ID_FOR_TEST ) );

    }

    @Test
    public void testdeleteKey() throws Exception {
        assertNotNull( Commands2.deleteKey( KEY_ID_FOR_TEST ) );

    }

    @Test
    public void testdeleteSubKey() throws Exception {
        assertNotNull( Commands2.deleteSubKey( SUBKEY_ID_FOR_TEST ));

    }

    @Test
    public void testrevokeKey() throws Exception {
        assertNotNull( Commands2.revokeKey( KEY_ID_FOR_TEST ) );

    }

    @Test
    public void testrevokeSubKey() throws Exception {
        assertNotNull( Commands2.revokeSubKey( SUBKEY_ID_FOR_TEST ) );

    }

    /*
    @Test
    public void testexecuteCommand() throws Exception {
        // nothing to test...
    }
    */
}

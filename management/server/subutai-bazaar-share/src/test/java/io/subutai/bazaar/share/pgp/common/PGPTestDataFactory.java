package io.subutai.bazaar.share.pgp.common;


import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;

import org.apache.commons.lang3.RandomStringUtils;

import io.subutai.bazaar.share.pgp.key.PGPKeyHelper;


public class PGPTestDataFactory
{
    private static final String KEYS_DIR = "src/test/resources/keys/";

    public static final String PUBLIC_KEY_OWNER = "alice";

    public static final String PUBLIC_KEY_FIGNERPRINT = "6831eaf43361e3aa116abc1541c24197044fd723";

    public static final String PUBLIC_KEY_PATH = "src/test/resources/keys/alice.public.gpg";

    public static final String PRIVATE_KEY_PATH = "src/test/resources/keys/alice.secret.gpg";

    public static final String DEFAULT_PASSWORD = "abc123";


    public static PGPPublicKey getPublicKey( String owner ) throws Exception
    {
        return PGPKeyHelper.readPublicKey( KEYS_DIR + owner + ".public.gpg" );
    }


    public static PGPPrivateKey getPrivateKey( String owner ) throws Exception
    {
        return PGPKeyHelper.readPrivateKey( KEYS_DIR + owner + ".secret.gpg", DEFAULT_PASSWORD );
    }


    public static byte[] getData() throws Exception
    {
        return RandomStringUtils.random( 10000 ).getBytes();
    }
}

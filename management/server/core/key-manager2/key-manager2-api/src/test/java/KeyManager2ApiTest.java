import io.subutai.core.key2.api.KeyManagerException;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by caveman on 10.08.2015.
 */
public class KeyManager2ApiTest
{
    private String errorMessage = "ERROR";
    @Test
    @Ignore
    public void testException() throws Exception
    {
        Exception cause = new Exception();
        KeyManagerException keyManagerException = new KeyManagerException( cause );
        assertEquals( cause, keyManagerException.getCause() );
        KeyManagerException keyManagerException1 = new KeyManagerException( errorMessage );
        assertEquals( errorMessage, keyManagerException1.getCause() );

    }
}

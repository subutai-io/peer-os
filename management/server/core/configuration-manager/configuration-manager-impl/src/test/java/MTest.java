import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.configuration.manager.impl.utils.TestUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by bahadyr on 7/9/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class MTest {

    @Test
    public void test() {
        TestUtil u = mock( TestUtil.class );
        when( u.testMethod() ).thenReturn( "hi" );
        System.out.println("TEST COMPLETE");
    }

    @Test
    public void anotherTestMethod() {
        System.out.println("ANOTHER TEST METHOD");
    }


}

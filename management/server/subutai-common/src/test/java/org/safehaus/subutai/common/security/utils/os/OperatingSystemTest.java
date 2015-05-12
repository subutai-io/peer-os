package org.safehaus.subutai.common.security.utils.os;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class OperatingSystemTest
{
    private OperatingSystem operatingSystem;

    @Before
    public void setUp() throws Exception
    {
    }


    @Test
    public void testProperties() throws Exception
    {
        OperatingSystem.isAix();
        OperatingSystem.isWindowsNt4();
        OperatingSystem.isWindows95();
        OperatingSystem.isWindows98();
        OperatingSystem.isWindowsMe();
        OperatingSystem.isWindows2000();
        OperatingSystem.isWindowsXp();
        OperatingSystem.isWindowsVista();
        OperatingSystem.isWindows7();
        OperatingSystem.isWindows8();
        OperatingSystem.isWindows();
        OperatingSystem.isLinux();
        OperatingSystem.isMacOs();
        OperatingSystem.isSolaris();
        OperatingSystem.isFreeBsd();
        OperatingSystem.isHpUx();
        OperatingSystem.isIrix();
        OperatingSystem.isDigitalUnix();
        OperatingSystem.isUnix();
        OperatingSystem.isOs2();
        OperatingSystem.isUnknown();

    }
}
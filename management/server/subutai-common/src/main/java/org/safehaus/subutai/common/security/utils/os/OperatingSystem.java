package org.safehaus.subutai.common.security.utils.os;


/**
 * Local operating system detection.
 */
public class OperatingSystem
{
    private static final String OS_NAME = System.getProperty( "os.name" );
    private static final String OS_VERSION = System.getProperty( "os.version" );


    // @formatter:off

	/*
     * OS detection is relatively simple for most platforms. Simply check that
	 * the 'os.name' system property contains a recognized string. Windows is
	 * the exception as the os name may be wrong, e.g. Windows 2000 is sometimes
	 * reported as 'Windows NT' We therefore check for the string 'Windows' only
	 * and use the 'os.version' system property to discriminate:
	 * 
	 * - NT4 : 4.0 - 95 : 4.0 - 98 : 4.1 - ME : 4.9 - 2000 : 5.0 - XP : 5.1 -
	 * Vista : 6.0 - 7: 6.1 - 8: 6.2 - 8.1: 6.3
	 * 
	 * This works find except for NT4 and 95 which have the same version. For
	 * these we also check for the full os name as well.
	 */

    // @formatter:on


    private OperatingSystem()
    {
    }


    /**
     * Is operating system Windows NT 4?
     *
     * @return True if it is
     */
    public static boolean isWindowsNt4()
    {
        return OS_NAME.contains( "Windows NT" ) && ( "4.0".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows 95?
     *
     * @return True if it is
     */
    public static boolean isWindows95()
    {
        return OS_NAME.contains( "Windows 95" ) && ( "4.0".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows 98?
     *
     * @return True if it is
     */
    public static boolean isWindows98()
    {
        return OS_NAME.contains( "Windows" ) && ( "4.1".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows ME?
     *
     * @return True if it is
     */
    public static boolean isWindowsMe()
    {
        return OS_NAME.contains( "Windows" ) && ( "4.9".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows 2000?
     *
     * @return True if it is
     */
    public static boolean isWindows2000()
    {
        return OS_NAME.contains( "Windows" ) && ( "5.0".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows XP?
     *
     * @return True if it is
     */
    public static boolean isWindowsXp()
    {
        return OS_NAME.contains( "Windows" ) && ( "5.1".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows Vista?
     *
     * @return True if it is
     */
    public static boolean isWindowsVista()
    {
        return OS_NAME.contains( "Windows" ) && ( "6.0".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows 7?
     *
     * @return True if it is
     */
    public static boolean isWindows7()
    {
        return OS_NAME.contains( "Windows" ) && ( "6.1".equals( OS_VERSION ) );
    }


    /**
     * Is operating system Windows 8?
     *
     * @return True if it is
     */
    public static boolean isWindows8()
    {
        return OS_NAME.contains( "Windows" ) && ( "6.2".equals( OS_VERSION ) || "6.3".equals( OS_VERSION ) );
    }


    /**
     * Is operating system one of the various Windows flavours?
     *
     * @return True if it is
     */
    public static boolean isWindows()
    {
        return OS_NAME.contains( "Windows" );
    }


    /**
     * Is operating system Linux?
     *
     * @return True if it is
     */
    public static boolean isLinux()
    {
        return OS_NAME.contains( "Linux" );
    }


    /**
     * Is operating system Mac OS?
     *
     * @return True if it is
     */
    public static boolean isMacOs()
    {
        return OS_NAME.contains( "Mac OS" );
    }


    /**
     * Is operating system Solaris?
     *
     * @return True if it is
     */
    public static boolean isSolaris()
    {
        return OS_NAME.contains( "Solaris" ) || ( OS_NAME.contains( "SunOS" ) );
    }


    /**
     * Is operating system AIX?
     *
     * @return True if it is
     */
    public static boolean isAix()
    {
        return OS_NAME.contains( "AIX" );
    }


    /**
     * Is operating system FreeBSD?
     *
     * @return True if it is
     */
    public static boolean isFreeBsd()
    {
        return OS_NAME.contains( "FreeBSD" );
    }


    /**
     * Is operating system HP-UX?
     *
     * @return True if it is
     */
    public static boolean isHpUx()
    {
        return OS_NAME.contains( "HP-UX" );
    }


    /**
     * Is operating system Irix?
     *
     * @return True if it is
     */
    public static boolean isIrix()
    {
        return OS_NAME.contains( "Irix" );
    }


    /**
     * Is operating system Digital UNIX?
     *
     * @return True if it is
     */
    public static boolean isDigitalUnix()
    {
        return OS_NAME.contains( "Digital Unix" );
    }


    /**
     * Is operating system one of the various Unix flavours?
     *
     * @return True if it is
     */
    public static boolean isUnix()
    {
        return isSolaris() || isAix() || isFreeBsd() || isHpUx() || isIrix() || isDigitalUnix();
    }


    /**
     * Is operating system OS/2?
     *
     * @return True if it is
     */
    public static boolean isOs2()
    {
        return OS_NAME.contains( "OS/2" );
    }


    /**
     * Is operating system unknown?
     *
     * @return True if it is
     */
    public static boolean isUnknown()
    {
        return !isWindows() && !isLinux() && !isMacOs() && !isUnix() && !isOs2();
    }
}

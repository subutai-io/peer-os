package io.subutai.bazaar.share;


public class Utils
{
    public static String buildSubutaiOrigin( final String environmentId, final String peerId,
                                             final String containerId )
    {
        return String.format( "%s.%s.%s", environmentId, peerId, containerId, environmentId );
    }
}

package io.subutai.core.test.cli;


import java.util.Map;

import com.google.common.collect.Maps;

import io.subutai.common.environment.PeerTemplatesDownloadProgress;
import io.subutai.common.environment.RhTemplatesDownloadProgress;
import io.subutai.common.util.JsonUtil;


public class Test
{

    @org.junit.Test
    public void name() throws Exception
    {
        Map<String, Integer> map1 = Maps.newHashMap();
        map1.put( "apache", 45 );
        map1.put( "casandra", 55 );
        Map<String, Integer> map2 = Maps.newHashMap();
        map2.put( "mongo", 45 );
        map2.put( "xk", 55 );
        RhTemplatesDownloadProgress rhTemplatesDownloadProgress = new RhTemplatesDownloadProgress( "rh1", map1 );
        RhTemplatesDownloadProgress rhTemplatesDownloadProgress2 = new RhTemplatesDownloadProgress( "rh2", map2 );

        PeerTemplatesDownloadProgress peerTemplatesDownloadProgress = new PeerTemplatesDownloadProgress( "peer123" );
        peerTemplatesDownloadProgress.addTemplateDownloadProgress( rhTemplatesDownloadProgress );
        peerTemplatesDownloadProgress.addTemplateDownloadProgress( rhTemplatesDownloadProgress2 );

        System.out.println( JsonUtil.toJson( peerTemplatesDownloadProgress ) );
    }
}

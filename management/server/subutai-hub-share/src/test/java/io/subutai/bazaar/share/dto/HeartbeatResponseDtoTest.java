package io.subutai.bazaar.share.dto;


import java.io.IOException;

import org.junit.Test;

import io.subutai.bazaar.share.json.JsonUtil;

import static org.junit.Assert.assertEquals;

public class HeartbeatResponseDtoTest
{
    @Test
    public void test() throws IOException
    {
        HeartbeatResponseDto res = new HeartbeatResponseDto();

        res.getStateLinks().add( "one" );
        res.getStateLinks().add( "two" );
        res.getStateLinks().add( "three" );

        byte[] data = JsonUtil.toCbor( res );

        HeartbeatResponseDto res2 = JsonUtil.fromCbor( data, HeartbeatResponseDto.class );

        assertEquals( res.getStateLinks(), res2.getStateLinks() );
    }
}

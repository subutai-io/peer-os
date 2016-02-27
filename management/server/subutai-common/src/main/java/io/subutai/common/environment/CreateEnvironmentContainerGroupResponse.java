package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class CreateEnvironmentContainerGroupResponse
{
    private Set<CloneResponse> responses = new CopyOnWriteArraySet<>();
    private String peerId;
    private AtomicInteger counter = new AtomicInteger( 0 );


    public CreateEnvironmentContainerGroupResponse( final String peerId )
    {
        this.peerId = peerId;
    }


    public void addResponse( CloneResponse response )
    {
        if ( response == null )
        {
            throw new IllegalArgumentException( "Clone response could not be null." );
        }
        responses.add( response );
        counter.incrementAndGet();
    }


    public Set<CloneResponse> getResponses()
    {
        return responses;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void waitResponses( final int count )
    {
        while ( counter.intValue() < count )
        {
            try
            {
                TimeUnit.SECONDS.sleep( 3 );
            }
            catch ( InterruptedException e )
            {
                //ignore
            }
        }
    }
}

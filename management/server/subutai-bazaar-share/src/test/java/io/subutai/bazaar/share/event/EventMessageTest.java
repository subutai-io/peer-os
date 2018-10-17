package io.subutai.bazaar.share.event;


import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.subutai.bazaar.share.event.meta.CustomMeta;
import io.subutai.bazaar.share.event.meta.OriginMeta;
import io.subutai.bazaar.share.event.meta.SourceMeta;
import io.subutai.bazaar.share.event.meta.TraceMeta;
import io.subutai.bazaar.share.event.payload.LogPayload;
import io.subutai.bazaar.share.event.payload.ProgressPayload;

import static org.junit.Assert.assertEquals;


public class EventMessageTest extends ObjectMapperTest
{

    public static final String FIRST_PLACE = "container";
    public static final String SECOND_PLACE = "peer";
    public static final String THIRD_PLACE = "bazaar";
    private ProgressPayload progressPayload;
    private OriginMeta origin;
    private SourceMeta source;
    private EventMessage originalObject;
    private LogPayload logPayload;


    @Before
    public void setup()
    {
        super.setup();
        progressPayload = new ProgressPayload( "step", "message", 99.0 );

        logPayload = new LogPayload("source", "initiated", LogPayload.Level.INFO);

        origin = new OriginMeta( "subutaiPeer.subutaiContainer.subutaiEnvironment" );

        source = new SourceMeta( "cassandra-blueprint", SourceMeta.Type.BLUEPRINT );
    }


    @Test
    public void testProgressPayloadSerializationAndDeserialization() throws IOException
    {
        originalObject = new EventMessage( origin, source, progressPayload );
        originalObject.addCustomMeta( new CustomMeta( "OS", "Linux" ) );
        originalObject.addCustomMeta( new CustomMeta( "bash", "sh" ) );

        final String json = objectMapper.writeValueAsString( originalObject );

        System.out.println( json );

        final EventMessage restoredObject = objectMapper.readValue( json, EventMessage.class );

        assertEquals( originalObject.getPayload(), restoredObject.getPayload() );
    }


    @Test
    public void testEventTrace()
    {
        originalObject = new EventMessage( origin, source, progressPayload );
        originalObject.addTrace( FIRST_PLACE );
        originalObject.addTrace( SECOND_PLACE );
        originalObject.addTrace( THIRD_PLACE );

        List<TraceMeta> trace = originalObject.getTrace();

        assertEquals( trace.get( 0 ).getPlace(), FIRST_PLACE );
        assertEquals( trace.get( 1 ).getPlace(), SECOND_PLACE );
        assertEquals( trace.get( 2 ).getPlace(), THIRD_PLACE );
    }

    @Test
    public void testLogPayloadSerializationAndDeserialization() throws IOException
    {
        originalObject = new EventMessage( origin, source, logPayload );

        final String json = objectMapper.writeValueAsString( originalObject );

        System.out.println( json );

        final EventMessage restoredObject = objectMapper.readValue( json, EventMessage.class );

        assertEquals( originalObject.getPayload(), restoredObject.getPayload() );
    }
}
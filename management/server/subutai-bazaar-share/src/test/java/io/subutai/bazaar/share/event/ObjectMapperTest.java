package io.subutai.bazaar.share.event;


import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ObjectMapperTest
{
    protected ObjectMapper objectMapper;


    @Before
    public void setup()
    {
        objectMapper = new ObjectMapper();
    }
}

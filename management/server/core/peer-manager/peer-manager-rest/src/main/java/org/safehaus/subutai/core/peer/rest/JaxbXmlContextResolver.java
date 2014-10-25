package org.safehaus.subutai.core.peer.rest;


import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.safehaus.subutai.common.protocol.Template;


/**
 * Created by timur on 10/24/14.
 */
public class JaxbXmlContextResolver implements ContextResolver<Object>
{
    private static final Class<?>[] classes = new Class[] { Template.class };
    private static final JAXBContext context = initContext();


    public static JAXBContext initContext()
    {
        JAXBContext context = null;
        try
        {
            context = JAXBContext.newInstance( classes );
        }
        catch ( JAXBException e )
        {
            throw new RuntimeException( e );
        }
        return context;
    }


    @Override
    public Object getContext( Class<?> arg0 )
    {
        return context;
    }
}
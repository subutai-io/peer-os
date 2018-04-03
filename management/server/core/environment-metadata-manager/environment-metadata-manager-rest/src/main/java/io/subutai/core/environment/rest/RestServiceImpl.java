package io.subutai.core.environment.rest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;


public class RestServiceImpl implements RestService {
    private static Logger LOG = LoggerFactory.getLogger(RestServiceImpl.class);

    @Override
    public Response issueToken(String containerIp) {
        return Response.ok("Under implementation").build();
    }
}

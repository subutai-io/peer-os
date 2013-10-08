package org.safehaus.kiskis.mgmt.shared.protocol.interfaces;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

/**
 * Used to generate {@link org.safehaus.kiskis.mgmt.shared.protocol.elements.Request} instances from various arguments
 */
public interface IResponseGenerator {

    public Response fromJson(String json);

    public String toJson(Response request);
}

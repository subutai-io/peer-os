package org.safehaus.kiskis.mgmt.shared.protocol.interfaces;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;

/**
 * Used to generate {@link Request} instances from various arguments
 */
public interface IRequestGenerator {

    public Request fromJson(String json);

    public String toJson(Request request);
}

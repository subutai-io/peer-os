package org.safehaus.kiskis.mgmt.shared.protocol.interfaces;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Transportable;

public interface IGenerator {

    public Transportable fromJson(String json);

    public String toJson(Transportable request);
}

package org.safehaus.kiskis.mgmt.shared.protocol.interfaces;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Command;

public interface IGenerator {

    public Command fromJson(String json);

    public String toJson(Command request);
}

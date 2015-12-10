package io.subutai.core.identity.api.model;


/**
 * Created by talas on 12/10/15.
 */
public interface TrustRelationship
{
    long getId();

    String getTrustLevel();

    String getContext();

    String getOperation();

    String getType();
}

package io.subutai.core.security.api.model;


/**
 * Created by talas on 12/10/15.
 */
public interface TrustRelationship
{
    long getId();

    String getTrustLevel();

    String getScope();

    String getAction();

    String getTtl();

    String getType();
}

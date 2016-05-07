package io.subutai.core.environment.impl.dao;


import java.util.Collection;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


public interface EnvironmentService
{
    EnvironmentImpl find( final String id );

    Collection<EnvironmentImpl> getAll();

    void persist( Environment item );

    void remove( final String id );

    EnvironmentImpl merge( EnvironmentImpl item );

    EnvironmentContainerImpl mergeContainer( EnvironmentContainerImpl container );
}

package io.subutai.core.environment.impl.dao;


import java.util.Collection;

import io.subutai.common.environment.Environment;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import io.subutai.core.environment.impl.entity.LocalEnvironment;


public interface EnvironmentService
{
    LocalEnvironment find( final String id );

    Collection<LocalEnvironment> getAll();

    Collection<LocalEnvironment> getDeleted();

    void persist( Environment item );

    void remove( final String id );

    LocalEnvironment merge( LocalEnvironment item );

    EnvironmentContainerImpl mergeContainer( EnvironmentContainerImpl container );
}

package io.subutai.common.peer;


import java.util.Set;

import io.subutai.common.environment.Environment;


public interface EnvironmentContainerHost extends ContainerHost
{
    void setEnvironment( Environment environment );

    void addTag( String tag );

    void removeTag( String tag );

    Set<String> getTags();
}

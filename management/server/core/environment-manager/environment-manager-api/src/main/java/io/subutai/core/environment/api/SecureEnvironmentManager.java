package io.subutai.core.environment.api;


import java.util.List;

import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.core.environment.api.ShareDto.ShareDto;


public interface SecureEnvironmentManager
{
    List<ShareDto> getSharedUsers( String objectId ) throws EnvironmentNotFoundException;

    void shareEnvironment( ShareDto[] shareDto, String environmentId );
}

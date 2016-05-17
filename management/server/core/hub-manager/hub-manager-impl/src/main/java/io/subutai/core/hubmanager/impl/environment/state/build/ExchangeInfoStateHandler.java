package io.subutai.core.hubmanager.impl.environment.state.build;


import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import io.subutai.common.security.objects.TokenType;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.environment.state.StateHandler;
import io.subutai.core.hubmanager.impl.processor.EnvironmentUserHelper;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;


public class ExchangeInfoStateHandler extends StateHandler
{
    public ExchangeInfoStateHandler( IdentityManager identityManager, EnvironmentUserHelper envUserHelper, ConfigManager configManager )
    {
        super( identityManager, envUserHelper, configManager );
    }


    @Override
    protected Object doHandle( EnvironmentPeerDto peerDto )
    {
        peerDto.setEnvOwnerToken( getEnvironmentOwnerToken( peerDto ) );



        return peerDto;
    }


    private String getEnvironmentOwnerToken( EnvironmentPeerDto peerDto )
    {
        User user = envUserHelper.handleEnvironmentOwnerCreation( peerDto );

        Date validDate = DateUtils.addYears( new Date(), 3 );

        UserToken token = identityManager.createUserToken( user, null, null, null, TokenType.Permanent.getId(), validDate );

        return token.getFullToken();
    }

}

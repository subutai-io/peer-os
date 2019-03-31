package io.subutai.core.localpeer.impl;


import org.apache.commons.lang3.StringUtils;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.settings.Common;


public class ResourceHostCommands
{
    public RequestBuilder getListContainerInfoCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai list info -n %s", containerName ) );
    }


    public RequestBuilder getListContainersInfoCommand()
    {
        return new RequestBuilder( "subutai list info" );
    }


    public RequestBuilder getListContainersCommand()
    {
        return new RequestBuilder( "subutai list containers" );
    }


    public RequestBuilder getStartContainerCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai start %s", containerName ) ).withTimeout( 1 ).daemon();
    }


    public RequestBuilder getStopContainerCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai stop %s", containerName ) )
                .withTimeout( Common.STOP_CONTAINER_TIMEOUT_SEC );
    }


    public RequestBuilder getDestroyContainerCommand( String containerId )
    {
        return new RequestBuilder( String.format( "subutai destroy id:%s", containerId ) )
                .withTimeout( Common.DESTROY_CONTAINER_TIMEOUT_SEC );
    }


    public RequestBuilder getCleanupEnvironmentCommand( int vlan )
    {
        return new RequestBuilder( String.format( "subutai cleanup %d", vlan ) )
                .withTimeout( Common.CLEANUP_ENV_TIMEOUT_SEC );
    }


    public RequestBuilder getFetchCpuCoresNumberCommand()
    {
        return new RequestBuilder( "nproc" );
    }


    public RequestBuilder getImportTemplateCommand( final String templateId )
    {
        return new RequestBuilder( String.format( "subutai import id:%s", templateId ) )
                .withTimeout( Common.TEMPLATE_DOWNLOAD_TIMEOUT_SEC );
    }


    public RequestBuilder getCloneContainerCommand( final String templateId, String containerName, String hostname,
                                                    String ip, int vlan, String environmentId, String containerToken )
    {
        return new RequestBuilder(
                String.format( "subutai clone id:%s %s -n \"%s %d\" -e %s -s %s && subutai hostname con %s %s",
                        templateId, containerName, ip, vlan, environmentId, containerToken, containerName, hostname ) )
                .withTimeout( Common.CLONE_TIMEOUT_SEC );
    }


    public RequestBuilder getExportTemplateCommand( final String containerName, final String templateName,
                                                    final String version, final boolean isPrivateTemplate,
                                                    final String token )
    {
        return new RequestBuilder(
                String.format( "subutai export %s --name %s --ver %s --token %s", containerName, templateName, version,
                        token ) ).withTimeout( Common.TEMPLATE_EXPORT_TIMEOUT_SEC );
    }


    public RequestBuilder getGetRhVersionCommand()
    {
        return new RequestBuilder( "subutai -v" );
    }


    public RequestBuilder getGetRhOsNameCommand()
    {
        return new RequestBuilder( "subutai info os" );
    }


    public RequestBuilder getGetVlanCommand()
    {
        return new RequestBuilder( "cat /var/lib/apps/subutai/current/vlan" );
    }


    public RequestBuilder getSetContainerHostnameCommand( final String containerName, final String newHostname )
    {
        return new RequestBuilder( String.format( "subutai hostname con %s %s", containerName, newHostname ) );
    }


    public RequestBuilder getGetSetRhHostnameCommand( final String newHostname )
    {
        return new RequestBuilder( String.format( "subutai hostname rh %s", newHostname ) );
    }


    public RequestBuilder getListContainerSnapshotsCommand( final String containerName )
    {
        return new RequestBuilder( String.format( "subutai snapshot list -c %s", containerName ) );
    }


    public RequestBuilder getRemoveContainerSnapshotCommand( final String containerName, final String partition,
                                                             String label )
    {
        return new RequestBuilder(
                String.format( "subutai snapshot remove -c %s -p %s -l %s", containerName, partition, label ) );
    }


    public RequestBuilder getRollbackContainerSnapshotCommand( final String containerName, final String partition,
                                                               final String label, final boolean force )
    {
        return new RequestBuilder(
                String.format( "subutai snapshot rollback -c %s -p %s -l %s %s", containerName, partition, label,
                        force ? "-f" : "" ) );
    }


    public RequestBuilder getAddContainerSnapshotCommand( final String containerName, final String partition,
                                                          final String label, final boolean stopContainer )
    {
        return new RequestBuilder(
                String.format( "subutai snapshot add -c %s -p %s -l %s %s", containerName, partition, label,
                        stopContainer ? "-s" : "" ) );
    }


    public RequestBuilder getDownloadRawFileFromCdnCommand( final String fileId, final String destinationDirectory )
    {
        return new RequestBuilder(
                String.format( "subutai cdn get %s --destination %s", fileId, destinationDirectory ) )
                .withTimeout( Common.TEMPLATE_DOWNLOAD_TIMEOUT_SEC );
    }


    public RequestBuilder getUploadRawFileToCdnCommand( final String pathToFile, final String cdnToken )
    {
        return new RequestBuilder( String.format( "subutai cdn put -f %s -t %s", pathToFile, cdnToken ) )
                .withTimeout( Common.TEMPLATE_EXPORT_TIMEOUT_SEC );
    }


    public RequestBuilder getSaveContainerSnapshotsCommand( final String containerName, final String label1,
                                                            final String label2, final String destinationDirectory )
    {
        return new RequestBuilder( String.format( "subutai snapshot send -c %s -l %s %s", containerName,
                StringUtils.isBlank( label2 ) ? label1 : label1 + "," + label2,
                StringUtils.isBlank( destinationDirectory ) ? "" : "--destination " + destinationDirectory ) )
                .withTimeout( Common.CONTAINER_DUMP_RECREATE_TIMEOUT_SEC );
    }


    public RequestBuilder getRecreateContainerFilesystemCommand( final String containerName, final String pathToFile )
    {
        return new RequestBuilder( String.format( "subutai snapshot recv -c %s -f %s", containerName, pathToFile ) )
                .withTimeout( Common.CONTAINER_DUMP_RECREATE_TIMEOUT_SEC );
    }


    public RequestBuilder getRecreateContainerCommand( final String containerName, final String hostname,
                                                       final String ip, final int vlan, final String environmentId,
                                                       final String containerToken )
    {
        return new RequestBuilder(
                String.format( "subutai restore %s -n \"%s %d\" -e %s -s %s && subutai hostname con %s %s",
                        containerName, ip, vlan, environmentId, containerToken, containerName, hostname ) )
                .withTimeout( Common.CLONE_TIMEOUT_SEC );
    }


    public RequestBuilder getGetTimezonOffsetCommand()
    {
        return new RequestBuilder( "date +\"%z\"" );
    }


    public RequestBuilder getCheckLxcExistsCommand( String name )
    {
        return new RequestBuilder( String.format( "subutai exists %s", name ) );
    }
}

package io.subutai.core.blueprint.auth;


import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import io.subutai.core.object.relation.api.BundlePorter;
import io.subutai.core.object.relation.api.RelationManager;
import io.subutai.core.object.relation.api.model.Relation;


public class BundlePorterImpl implements BundlePorter
{
    private ServiceTracker<RelationManager, RelationManager> serviceTracker;


    public BundlePorterImpl()
    {
        BundleContext ctx = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
        serviceTracker = new ServiceTracker<>( ctx,RelationManager.class,null );
        serviceTracker.open();
    }


    public RelationManager getRelationManager()
    {
        return serviceTracker.getService();
    }


    @Override
    public String getPort()
    {
        String result = "";
        List<Relation> relations = getRelationManager().getRelations();
        for ( final Relation relation : relations )
        {
            result += relation.toString() + "\n";
        }
        return result;
    }
}

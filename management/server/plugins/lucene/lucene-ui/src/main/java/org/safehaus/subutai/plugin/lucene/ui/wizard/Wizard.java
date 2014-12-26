package org.safehaus.subutai.plugin.lucene.ui.wizard;


import java.util.concurrent.ExecutorService;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.plugin.hadoop.api.Hadoop;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.lucene.api.Lucene;
import org.safehaus.subutai.plugin.lucene.api.LuceneConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;


public class Wizard
{

    private final GridLayout grid;
    private final ExecutorService executorService;
    private final Hadoop hadoop;
    private final Lucene lucene;
    private final Tracker tracker;
    private final EnvironmentManager environmentManager;
    private int step = 1;
    private LuceneConfig config = new LuceneConfig();
    private HadoopClusterConfig hadoopConfig;


    public Wizard( ExecutorService executorService, Lucene lucene, Hadoop hadoop, Tracker tracker, EnvironmentManager environmentManager ) throws NamingException
    {

        this.executorService = executorService;
        this.lucene = lucene;
        this.hadoop = hadoop;
        this.tracker = tracker;
        this.environmentManager = environmentManager;


        grid = new GridLayout( 1, 20 );
        grid.setMargin( true );
        grid.setSizeFull();

        putForm();
    }


    private void putForm()
    {
        grid.removeComponent( 0, 1 );
        Component component = null;
        switch ( step )
        {
            case 1:
            {
                component = new WelcomeStep( this );
                break;
            }
            case 2:
            {
                component = new ConfigurationStep( hadoop, this, environmentManager );
                break;
            }
            case 3:
            {
                component = new VerificationStep( lucene, executorService, tracker, environmentManager, this );
                break;
            }
            default:
            {
                break;
            }
        }

        if ( component != null )
        {
            grid.addComponent( component, 0, 1, 0, 19 );
        }
    }


    public Component getContent()
    {
        return grid;
    }


    protected void next()
    {
        step++;
        putForm();
    }


    protected void back()
    {
        step--;
        putForm();
    }


    protected void init()
    {
        step = 1;
        config = new LuceneConfig();
        putForm();
    }


    public LuceneConfig getConfig()
    {
        return config;
    }


    public HadoopClusterConfig getHadoopConfig()
    {
        return hadoopConfig;
    }


    public void setHadoopConfig( HadoopClusterConfig hadoopConfig )
    {
        this.hadoopConfig = hadoopConfig;
    }
}

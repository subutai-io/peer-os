package org.safehaus.subutai.core.monitor.ui;


import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.Monitoring;
import org.safehaus.subutai.core.monitor.ui.util.UIUtil;
import org.safehaus.subutai.server.ui.component.AgentTree;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;


public class MonitoringComponent extends CustomComponent
{

    private static final int MAX_SIZE = 500;
    private final Monitoring monitoring;
    private final AgentManager agentManager;
    private transient Chart chart;
    private AgentTree agentTree;
    private PopupDateField startDateField;
    private PopupDateField endDateField;
    private ListSelect metricListSelect;


    public MonitoringComponent( Monitoring monitoring, AgentManager agentManager )
    {
        this.monitoring = monitoring;
        this.agentManager = agentManager;
        initContent();
    }


    private void initContent()
    {

        setHeight( "100%" );

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition( 20 );

        agentTree = new AgentTree( agentManager );
        splitPanel.setFirstComponent( agentTree );
        splitPanel.setSecondComponent( getMainLayout() );

        setCompositionRoot( splitPanel );
    }


    private Layout getMainLayout()
    {

        AbsoluteLayout layout = new AbsoluteLayout();
        layout.setWidth( 1200, Unit.PIXELS );
        layout.setHeight( 1000, Unit.PIXELS );

        addNote( layout );
        addDateFields( layout );
        addMetricList( layout );
        addSubmitButton( layout );
        addChartLayout( layout );

        return layout;
    }


    private void addNote( AbsoluteLayout layout )
    {
        UIUtil.addLabel( layout, String.format( "<i>Note: the chart displays only recent %s values.</i>", MAX_SIZE ),
                "left: 20px; top: 10px;" );
    }


    private void addDateFields( AbsoluteLayout layout )
    {

        Date endDate = new Date();
        Date startDate = DateUtils.addHours( endDate, -1 );

        startDateField = UIUtil.addDateField( layout, "From:", "left: 20px; top: 50px;", startDate );
        endDateField = UIUtil.addDateField( layout, "To:", "left: 20px; top: 100px;", endDate );
    }


    private void addMetricList( AbsoluteLayout layout )
    {

        metricListSelect = UIUtil.addListSelect( layout, "Metric:", "left: 20px; top: 150px;", "200px", "270px" );

        for ( Metric metric : Metric.values() )
        {
            metricListSelect.addItem( metric );
        }
    }


    private void addSubmitButton( AbsoluteLayout layout )
    {

        Button button = UIUtil.getButton( "Submit", "150px" );

        button.addClickListener( new Button.ClickListener()
        {
            @Override
            public void buttonClick( Button.ClickEvent clickEvent )
            {
                submitButtonClicked();
            }
        } );

        layout.addComponent( button, "left: 20px; top: 430px;" );
    }


    private void submitButtonClicked()
    {

        String host = getSelectedNode();
        Metric metric = getSelectedMetric();

        if ( validParams( host, metric ) )
        {
            loadChart( host, metric );
        }
    }


    private String getSelectedNode()
    {

        Set<Agent> agents = agentTree.getSelectedAgents();

        return agents == null || agents.isEmpty() ? null : agents.iterator().next().getHostname();
    }


    private Metric getSelectedMetric()
    {
        return ( Metric ) metricListSelect.getValue();
    }


    private boolean validParams( String host, Metric metric )
    {

        boolean success = true;

        if ( StringUtils.isEmpty( host ) )
        {
            Notification.show( "Please select a node" );
            success = false;
        }
        else if ( metric == null )
        {
            Notification.show( "Please select a metric" );
            success = false;
        }

        return success;
    }


    private void loadChart( String host, Metric metric )
    {

        if ( chart == null )
        {
            chart = new Chart( MAX_SIZE );
        }

        Date startDate = startDateField.getValue();
        Date endDate = endDateField.getValue();

        Map<Date, Double> values = monitoring.getData( host, metric, startDate, endDate );
        chart.load( host, metric, values );
    }


    private void addChartLayout( AbsoluteLayout layout )
    {

        AbsoluteLayout chartLayout = new AbsoluteLayout();
        chartLayout.setWidth( 800, Unit.PIXELS );
        chartLayout.setHeight( 400, Unit.PIXELS );
        chartLayout.setDebugId( "chart" );

        layout.addComponent( chartLayout, "left: 250px; top: 50px;" );
    }
}

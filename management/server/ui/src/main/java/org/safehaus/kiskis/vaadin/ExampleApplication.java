package org.safehaus.kiskis.vaadin;

import com.vaadin.Application;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import org.safehaus.kiskis.vaadin.util.AppData;
import org.safehaus.kiskismgmt.protocol.Agent;

import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("serial")
public class ExampleApplication extends Application {

    private HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
    private Tree tree = new Tree("Settings");
    //
    private final String title;
    private Set<Agent> hosts;

    ExampleApplication(String title) {
        this.title = title;
    }

    @Override
    public void init() {
        // Create the application data instance
        AppData sessionData = new AppData(this);

        // Register it as a listener in the application context
        getContext().addTransactionListener(sessionData);

        setTheme("runo");
        buildMainLayout();
    }

    private void buildMainLayout() {
        setMainWindow(new Window(title));

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.addComponent(horizontalSplit);

        /* Allocate all available extra space to the horizontal split panel */
        layout.setExpandRatio(horizontalSplit, 1);

        /* Set the initial split position so we can have a 200 pixel menu to the left */
        horizontalSplit.setSplitPosition(200, SplitPanel.UNITS_PIXELS);
        horizontalSplit.setFirstComponent(initTree());
        horizontalSplit.setSecondComponent(getMainPanel());

        getMainWindow().setContent(layout);
    }

    private Tree initTree() {
        hosts = AppData.getBroker().getRegisteredHosts();
        Iterator itr = hosts.iterator();
        while(itr.hasNext()) {
            Agent agent = (Agent) itr.next();
            tree.addItem(agent.getUuid());
        }
        return tree;
    }

    private VerticalLayout getMainPanel() {
        VerticalLayout l = new VerticalLayout();
        l.setSizeFull();
        //l.addComponent(new TabView());

        return l;
    }
}

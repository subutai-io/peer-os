package org.safehaus.subutai.ui.mahout.manager;

import com.google.common.base.Strings;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.mahout.Config;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.ui.mahout.MahoutUI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;


public class AddNodeWindow extends Window {

	private final TextArea outputTxtArea;
	private final Label indicator;

    private final ListSelect nodesList = new ListSelect();
    private final String clusterName;

    private final ArrayList<String> selectedNodes = new ArrayList<>();

	public AddNodeWindow(final Config config, Set<Agent> nodes) {

		super("Add New Node");

        clusterName = config.getClusterName();

		setModal(true);

		setWidth( 700, Unit.PIXELS );
		setHeight( 500, Unit.PIXELS );

		GridLayout content = new GridLayout(1, 3);
		content.setSizeFull();
		content.setMargin(true);
		content.setSpacing(true);

		HorizontalLayout topContent = new HorizontalLayout();
		topContent.setSpacing(true);

		content.addComponent(topContent);
		topContent.addComponent(new Label("Nodes:"));

        initNodesList( nodes );
        topContent.addComponent( nodesList );

		final Button addNodeBtn = new Button("Add");
		addNodeBtn.addStyleName("default");
		topContent.addComponent(addNodeBtn);

		addNodeBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
                addButtonClicked();
			}
		});

		outputTxtArea = new TextArea("Operation output");
		outputTxtArea.setRows(13);
		outputTxtArea.setColumns(43);
		outputTxtArea.setImmediate(true);
		outputTxtArea.setWordwrap(true);

		content.addComponent(outputTxtArea);

		indicator = new Label();
		indicator.setIcon(new ThemeResource("img/spinner.gif"));
		indicator.setContentMode(ContentMode.HTML);
		indicator.setHeight(11, Unit.PIXELS);
		indicator.setWidth(50, Unit.PIXELS);
		indicator.setVisible(false);

		Button ok = new Button("Ok");
		ok.addStyleName("default");
		ok.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				close();
			}
		});

		HorizontalLayout bottomContent = new HorizontalLayout();
		bottomContent.addComponent(indicator);
		bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
		bottomContent.addComponent(ok);

		content.addComponent(bottomContent);
		content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

		setContent(content);
	}


    private void initNodesList( Set<Agent> nodes ) {
        nodesList.setRows( 5 );
        nodesList.setMultiSelect( true );
        nodesList.setNullSelectionAllowed( false );

        for ( Agent node : nodes ) {
            nodesList.addItem( node.getHostname() );
        }
    }


    private void setSelectedNodes() {

        selectedNodes.clear();

        for ( Iterator i = nodesList.getItemIds().iterator(); i.hasNext(); ) {
            Object id = i.next();
            if ( nodesList.isSelected( id ) )
            {
               selectedNodes.add( (String) id );
            }
        }
    }


    private void addNode( String hostname ) {

        showProgress();

        final UUID trackID = MahoutUI.getMahoutManager().addNode( clusterName, hostname );

        MahoutUI.getExecutor().execute( new Runnable() {
            public void run() {
                while ( true ) {
                    ProductOperationView po = MahoutUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
                    if (po != null) {
                        setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                        if (po.getState() != ProductOperationState.RUNNING) {
                            hideProgress();
                            break;
                        }
                    } else {
                        setOutput("Product operation not found. Check logs");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }

                addSelectedNodes();
            }
        });

    }


    private void addButtonClicked() {
        setSelectedNodes();
        addSelectedNodes();
    }


    private void addSelectedNodes() {
        if ( selectedNodes.isEmpty() ) {
            return;
        }

        String hostname = selectedNodes.get( 0 );
        selectedNodes.remove( 0 );

        addNode( hostname );
    }


	@Override
	public void close() {
		super.close();
	}

	private void showProgress() {
		indicator.setVisible(true);
	}

	private void hideProgress() {
		indicator.setVisible(false);
	}

	private void setOutput(String output) {
		if (!Strings.isNullOrEmpty(output)) {
			outputTxtArea.setValue(output);
			outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
		}
	}

}

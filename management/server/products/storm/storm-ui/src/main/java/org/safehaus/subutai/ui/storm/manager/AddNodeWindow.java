package org.safehaus.subutai.ui.storm.manager;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.storm.Config;
import org.safehaus.subutai.shared.operation.ProductOperationState;
import org.safehaus.subutai.shared.operation.ProductOperationView;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;
import org.safehaus.subutai.ui.storm.StormUI;

import java.util.Set;
import java.util.UUID;

class AddNodeWindow extends Window {

	private final TextArea outputTxtArea;
	private final Button ok;
	private final Label indicator;
	private volatile boolean track = true;

	public AddNodeWindow(final Config config, Set<Agent> nodes) {
		super("Add New Node");
		setModal(true);
		setWidth(600, Unit.PIXELS);

		GridLayout content = new GridLayout(1, 3);
		content.setSizeFull();
		content.setMargin(true);
		content.setSpacing(true);

		HorizontalLayout topContent = new HorizontalLayout();
		topContent.setSpacing(true);

		content.addComponent(topContent);
		topContent.addComponent(new Label("Nodes:"));

		final ComboBox availableNodes = new ComboBox();
		availableNodes.setImmediate(true);
		availableNodes.setTextInputAllowed(false);
		availableNodes.setNullSelectionAllowed(false);
		availableNodes.setRequired(true);
		availableNodes.setWidth(200, Unit.PIXELS);
		for (Agent node : nodes) {
			availableNodes.addItem(node);
			availableNodes.setItemCaption(node, node.getHostname());
		}
		availableNodes.setValue(nodes.iterator().next());

		final Button addNodeBtn = new Button("Add");
		addNodeBtn.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				addNodeBtn.setEnabled(false);
				showProgress();
				Agent agent = (Agent) availableNodes.getValue();
				final UUID trackId = StormUI.getManager().addNode(
						config.getClusterName(), agent.getHostname());
				StormUI.getExecutor().execute(new Runnable() {

					public void run() {
						ProductOperationView po;
						while (track) {
							po = StormUI.getTracker().getProductOperation(
									Config.PRODUCT_NAME, trackId);
							if (po != null) {
								setOutput(po.getDescription() + "\nState: "
										+ po.getState() + "\nLogs:\n" + po.getLog());
								if (po.getState() != ProductOperationState.RUNNING) {
									hideProgress();
									break;
								}
							} else
								setOutput("Product operation not found. Check logs");
							try {
								Thread.sleep(300);
							} catch (InterruptedException ex) {
								break;
							}
						}
					}
				});
			}
		});

		topContent.addComponent(availableNodes);
		topContent.addComponent(addNodeBtn);

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

		ok = new Button("Ok");
		ok.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(Button.ClickEvent event) {
				//close window
				track = false;
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

	private void showProgress() {
		indicator.setVisible(true);
		ok.setEnabled(false);
	}

	private void setOutput(String output) {
		if (!Util.isStringEmpty(output)) {
			outputTxtArea.setValue(output);
			outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
		}
	}

	private void hideProgress() {
		indicator.setVisible(false);
		ok.setEnabled(true);
	}

	@Override
	public void close() {
		super.close();
		track = false;
	}

}

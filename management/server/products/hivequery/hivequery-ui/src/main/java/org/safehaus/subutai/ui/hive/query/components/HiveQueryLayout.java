package org.safehaus.subutai.ui.hive.query.components;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.api.hive.query.Config;
import org.safehaus.subutai.common.tracker.ProductOperationState;
import org.safehaus.subutai.common.tracker.ProductOperationView;
import org.safehaus.subutai.ui.hive.query.HiveQueryUI;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by daralbaev on 12.05.14.
 */
public class HiveQueryLayout extends GridLayout {

	private HadoopTreeTable table;
	private QueryList list;
	private TextField searchTextField;
	private TextField nameTextField;
	private TextArea queryTextArea;
	private TextArea descriptionTextArea;
	private TextArea resultTextArea;
	private HorizontalLayout buttonLayout;
	private Button saveButton, runButton;
	private Embedded indicator;

	public HiveQueryLayout() {
		super(12, 12);

		setSpacing(true);
		setSizeFull();

		getHadoopTable();
		getSearchField();
		getQueryList();
		getQueryNameField();
		getButtonLayout();
		getQueryField();
		getDescriptionField();
		getResultField();
	}

	private void getHadoopTable() {
		table = new HadoopTreeTable();
		addComponent(table, 0, 0, 5, 3);
		setComponentAlignment(table, Alignment.MIDDLE_CENTER);
	}

	private void getSearchField() {
		searchTextField = new TextField("Search");
		addComponent(searchTextField, 6, 0, 11, 0);
		setComponentAlignment(searchTextField, Alignment.MIDDLE_CENTER);
		searchTextField.setSizeFull();
		searchTextField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
		searchTextField.setTextChangeTimeout(200);
		searchTextField.addTextChangeListener(new FieldEvents.TextChangeListener() {
			@Override
			public void textChange(FieldEvents.TextChangeEvent event) {
				list.refreshDataSource(event.getText());
			}
		});
	}

	private void getQueryList() {
		list = new QueryList();
		addComponent(list, 6, 1, 11, 3);
		setComponentAlignment(list, Alignment.MIDDLE_CENTER);
		list.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (list.getValue() != null) {
					Config item = (Config) list.getValue();

					nameTextField.setValue(item.getName());
					queryTextArea.setValue(item.getQuery());
					descriptionTextArea.setValue(item.getDescription());
				}
			}
		});
	}

	private void getQueryNameField() {
		nameTextField = new TextField("Query Name");
		addComponent(nameTextField, 0, 4, 5, 4);
		setComponentAlignment(nameTextField, Alignment.MIDDLE_CENTER);
		nameTextField.setSizeFull();
	}

	private void getButtonLayout() {
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		getSaveButton();
		getRunButton();

		buttonLayout.addComponent(runButton);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(getIndicator());
		addComponent(buttonLayout, 6, 4, 11, 4);
		setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
	}

	private void getQueryField() {
		queryTextArea = new TextArea("Query");
		addComponent(queryTextArea, 0, 5, 5, 7);
		setComponentAlignment(queryTextArea, Alignment.MIDDLE_CENTER);
		queryTextArea.setSizeFull();
	}

	private void getDescriptionField() {
		descriptionTextArea = new TextArea("Description");
		addComponent(descriptionTextArea, 6, 5, 11, 7);
		setComponentAlignment(descriptionTextArea, Alignment.MIDDLE_CENTER);
		descriptionTextArea.setSizeFull();
	}

	private void getResultField() {
		resultTextArea = new TextArea("Result");
		addComponent(resultTextArea, 0, 8, 11, 11);
		setComponentAlignment(resultTextArea, Alignment.MIDDLE_CENTER);
		resultTextArea.setSizeFull();
	}

	private void getSaveButton() {
		saveButton = new Button("Save");
		saveButton.addStyleName("default");
		saveButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				HiveQueryUI.getManager().save(
						nameTextField.getValue().toString(),
						queryTextArea.getValue().toString(),
						descriptionTextArea.getValue().toString());

				searchTextField.setValue("");
				list.refreshDataSource(null);
			}
		});
	}

	private void getRunButton() {
		runButton = new Button("Run");
		runButton.addStyleName("default");
		runButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				resultTextArea.setValue("");
				indicator.setVisible(true);

				Collection<?> items = ((Collection<?>) table.getValue());
				for (Object item : items) {
					AgentContainer agentContainer = (AgentContainer) table.getContainerProperty(item, HadoopTreeTable.NODE_NAME_PROPERTY).getValue();
					runQuery(agentContainer.getAgent().getHostname());
				}

				searchTextField.setValue("");
				list.refreshDataSource(null);
			}
		});
	}

	private Embedded getIndicator() {
		indicator = new Embedded("", new ThemeResource("img/spinner.gif"));
		indicator.setHeight(11, Unit.PIXELS);
		indicator.setWidth(50, Unit.PIXELS);
		indicator.setVisible(false);

		return indicator;
	}

	private void runQuery(String hostname) {
		final UUID trackID = HiveQueryUI.getManager().run(hostname, queryTextArea.getValue().toString());

		HiveQueryUI.getExecutor().execute(new Runnable() {

			public void run() {
				long start = System.currentTimeMillis();
				while (!Thread.interrupted()) {
					ProductOperationView po = HiveQueryUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
					if (po != null) {
						if (po.getState() != ProductOperationState.RUNNING) {
							resultTextArea.setValue(String.format("%s\n%s", resultTextArea.getValue(), po.getLog()));
							indicator.setVisible(false);
							break;
						}
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						break;
					}
					if (System.currentTimeMillis() - start > (30 + 3) * 1000) {
						break;
					}
				}
			}
		});
	}
}

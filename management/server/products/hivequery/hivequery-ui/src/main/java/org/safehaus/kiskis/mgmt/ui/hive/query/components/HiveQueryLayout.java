package org.safehaus.kiskis.mgmt.ui.hive.query.components;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.hive.query.Config;
import org.safehaus.kiskis.mgmt.ui.hive.query.HiveQueryUI;

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

    public HiveQueryLayout() {
        super(12, 12);

        setSpacing(true);
        setSizeFull();

        table = new HadoopTreeTable();
        addComponent(table, 0, 0, 5, 3);
        setComponentAlignment(table, Alignment.MIDDLE_CENTER);

        searchTextField = new TextField("Search");
        addComponent(searchTextField, 6, 0, 11, 0);
        setComponentAlignment(searchTextField, Alignment.MIDDLE_CENTER);
        searchTextField.setSizeFull();
        searchTextField.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        searchTextField.setTextChangeTimeout(200);
        searchTextField.addListener(new FieldEvents.TextChangeListener() {

            public void textChange(FieldEvents.TextChangeEvent event) {
                list.refreshDataSource(event.getText());
            }
        });

        list = new QueryList();
        addComponent(list, 6, 1, 11, 3);
        setComponentAlignment(list, Alignment.MIDDLE_CENTER);
        list.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (valueChangeEvent.getProperty() != null) {
                    Config item = (Config) list.getValue();

                    nameTextField.setValue(item.getName());
                    queryTextArea.setValue(item.getQuery());
                    descriptionTextArea.setValue(item.getDescription());
                }
            }
        });

        nameTextField = new TextField("Query Name");
        addComponent(nameTextField, 0, 4, 5, 4);
        setComponentAlignment(nameTextField, Alignment.MIDDLE_CENTER);
        nameTextField.setSizeFull();

        buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setSizeFull();

        saveButton = new Button("Save");
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                HiveQueryUI.getManager().save(
                        nameTextField.getValue().toString(),
                        queryTextArea.getValue().toString(),
                        descriptionTextArea.getValue().toString());

                list.refreshDataSource(searchTextField.getValue());
            }
        });

        runButton = new Button("Run");

        buttonLayout.addComponent(runButton);
        buttonLayout.addComponent(saveButton);
        addComponent(buttonLayout, 6, 4, 11, 4);
        setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);


        queryTextArea = new TextArea("Query");
        addComponent(queryTextArea, 0, 5, 5, 7);
        setComponentAlignment(queryTextArea, Alignment.MIDDLE_CENTER);
        queryTextArea.setSizeFull();

        descriptionTextArea = new TextArea("Description");
        addComponent(descriptionTextArea, 6, 5, 11, 7);
        setComponentAlignment(descriptionTextArea, Alignment.MIDDLE_CENTER);
        descriptionTextArea.setSizeFull();

        resultTextArea = new TextArea("Result");
        addComponent(resultTextArea, 0, 8, 11, 11);
        setComponentAlignment(resultTextArea, Alignment.MIDDLE_CENTER);
        resultTextArea.setSizeFull();
    }
}

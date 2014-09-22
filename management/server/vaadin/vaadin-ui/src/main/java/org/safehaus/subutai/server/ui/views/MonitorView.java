/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package org.safehaus.subutai.server.ui.views;



import org.safehaus.subutai.common.protocol.Parameters;
import org.safehaus.subutai.common.protocol.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.vaadin.data.Property;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


public class MonitorView extends VerticalLayout implements View
{

    private static String jsonString = "{\n" +
            "    \"parameters\": [\n" +
            "        {\n" +
            "            \"file\": \"core-site.xml\",\n" +
            "            \"type\": \"xml\",\n" +
            "            \"fieldName\": \"fs.default.name\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"FS Default Name\",\n" +
            "            \"required\": true,\n" +
            "            \"tooltip\": \"Enter fs default name\",\n" +
            "            \"uiType\": \"TextField\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"hdfs-site.xml\",\n" +
            "            \"type\": \"xml\",\n" +
            "            \"fieldName\": \"hadoop.tmp.dir\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Hadoop Temp Dir\",\n" +
            "            \"required\": false,\n" +
            "            \"tooltip\": \"Enter replication factor\",\n" +
            "            \"uiType\": \"TextField\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"hdfs-site.xml\",\n" +
            "            \"type\": \"xml\",\n" +
            "            \"fieldName\": \"dfs.replication\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Replication\",\n" +
            "            \"required\": true,\n" +
            "            \"tooltip\": \"Enter excluded nodes, one per line\",\n" +
            "            \"uiType\": \"TextField\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"dfs.include\",\n" +
            "            \"type\": \"plain\",\n" +
            "            \"fieldName\": \"\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Include\",\n" +
            "            \"required\": false,\n" +
            "            \"tooltip\": \"Enter included nodes, one per line\",\n" +
            "            \"uiType\": \"TextArea\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"dfs.exclude\",\n" +
            "            \"type\": \"plain\",\n" +
            "            \"fieldName\": \"\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Include\",\n" +
            "            \"required\": false,\n" +
            "            \"tooltip\": \"Enter excluded nodes, one per line\",\n" +
            "            \"uiType\": \"TextArea\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"masters\",\n" +
            "            \"type\": \"plain\",\n" +
            "            \"fieldName\": \"\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Masters\",\n" +
            "            \"required\": false,\n" +
            "            \"tooltip\": \"Enter masters, one per line\",\n" +
            "            \"uiType\": \"TextArea\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"slaves\",\n" +
            "            \"type\": \"plain\",\n" +
            "            \"fieldName\": \"\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Slaves\",\n" +
            "            \"required\": false,\n" +
            "            \"tooltip\": \"Enter slaves, one per line\",\n" +
            "            \"uiType\": \"TextArea\",\n" +
            "            \"value\": null\n" +
            "        },\n" +
            "        {\n" +
            "            \"file\": \"mapred-site.xml\",\n" +
            "            \"type\": \"xml\",\n" +
            "            \"fieldName\": \"mapred.job.tracker\",\n" +
            "            \"fieldPath\": \"\",\n" +
            "            \"label\": \"Job Tracker\",\n" +
            "            \"required\": true,\n" +
            "            \"tooltip\": \"Enter Job Tracker\",\n" +
            "            \"uiType\": \"TextField\",\n" +
            "            \"value\": null\n" +
            "        }\n" +
            "    ]\n" +
            "}";
    private static final Logger LOG = LoggerFactory.getLogger( MonitorView.class.getName() );


    @Override
    public void enter( ViewChangeEvent event )
    {
        setSizeFull();
        addStyleName( "reports" );

        VerticalLayout panel = new VerticalLayout();
        panel.addStyleName( "dynamic-form" );
        panel.setSizeFull();
        panel.setMargin( true );
        panel.setSpacing( true );

        addComponent( panel );

        final Gson gson = new Gson();
        try
        {
            final Parameters param = gson.fromJson( jsonString, Parameters.class );
            for ( final Setting field : param.parameters )
            {
                if ( field.uiType.equals( "TextField" ) )
                {
                    final TextField textField = new TextField();
                    textField.setWidth( 100, Unit.PERCENTAGE );
                    textField.setCaption( field.label );
                    textField.setInputPrompt( field.tooltip );
                    textField.setDescription( field.tooltip );
                    textField.setImmediate( true );
                    if ( field.value != null )
                    {
                        textField.setValue( field.value );
                    }
                    panel.addComponent( textField );

                    textField.addValueChangeListener( new Property.ValueChangeListener()
                    {
                        @Override
                        public void valueChange( Property.ValueChangeEvent valueChangeEvent )
                        {
                            field.value = textField.getValue();
                        }
                    } );
                }
                else
                {
                    final TextArea textArea = new TextArea();
                    textArea.setWidth( 100, Unit.PERCENTAGE );
                    textArea.setCaption( field.label );
                    textArea.setInputPrompt( field.tooltip );
                    textArea.setDescription( field.tooltip );
                    textArea.setImmediate( true );
                    if ( field.value != null )
                    {
                        textArea.setValue( field.value );
                    }
                    panel.addComponent( textArea );

                    textArea.addValueChangeListener( new Property.ValueChangeListener()
                    {
                        @Override
                        public void valueChange( Property.ValueChangeEvent valueChangeEvent )
                        {
                            field.value = textArea.getValue();
                        }
                    } );
                }
            }

            Button button = new Button( "Export" );
            button.addStyleName( "default" );
            panel.addComponent( button );

            button.addClickListener( new Button.ClickListener()
            {
                @Override
                public void buttonClick( Button.ClickEvent clickEvent )
                {
                    System.out.println( gson.toJson( param ) );
                }
            } );
        }
        catch ( Exception ex )
        {
            LOG.info( ex.getMessage() );
        }
    }
}

package org.safehaus.subutai.core.peer.ui;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.peer.ui.forms.PeerRegisterForm;


/**
 * Created by bahadyr on 8/28/14.
 */
public class PeerForm extends CustomComponent implements Disposable {

    private PeerUI peerUI;


    public PeerForm(PeerUI peerUI) {
        setHeight(100, UNITS_PERCENTAGE);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.setSizeFull();

        TabSheet sheet = new TabSheet();
        sheet.setStyleName(Runo.TABSHEET_SMALL);
        sheet.setSizeFull();
        sheet.addTab(new PeerRegisterForm(peerUI), "Registration");
        verticalLayout.addComponent(sheet);


        setCompositionRoot(verticalLayout);
    }


    @Override
    public void dispose() {
        peerUI = null;
    }
}

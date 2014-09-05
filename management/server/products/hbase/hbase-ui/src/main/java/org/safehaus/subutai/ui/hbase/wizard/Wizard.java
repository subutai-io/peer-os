/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.ui.hbase.wizard;


import org.safehaus.subutai.api.hbase.HBaseConfig;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;


/**
 * @author dilshat
 */
public class Wizard {

    private final VerticalLayout vlayout;
    private int step = 1;
    private HBaseConfig config = new HBaseConfig();


    public Wizard() {
        vlayout = new VerticalLayout();
        vlayout.setSizeFull();
        vlayout.setMargin( true );
        putForm();
    }


    private void putForm() {
        vlayout.removeAllComponents();
        switch ( step ) {
            case 1: {
                vlayout.addComponent( new StepStart( this ) );
                break;
            }
            case 2: {
                vlayout.addComponent( new ConfigurationStep( this ) );
                break;
            }
            case 3: {
                vlayout.addComponent( new StepSetMaster( this ) );
                break;
            }
            case 4: {
                vlayout.addComponent( new StepSetRegion( this ) );
                break;
            }
            case 5: {
                vlayout.addComponent( new StepSetQuorum( this ) );
                break;
            }
            case 6: {
                vlayout.addComponent( new StepSetBackupMasters( this ) );
                break;
            }
            case 7: {
                vlayout.addComponent( new VerificationStep( this ) );
                break;
            }
            default: {
                step = 1;
                vlayout.addComponent( new StepStart( this ) );
                break;
            }
        }
    }


    public Component getContent() {
        return vlayout;
    }


    public void next() {
        step++;
        putForm();
    }


    public void back() {
        step--;
        putForm();
    }


    public void cancel() {
        step = 1;
        putForm();
    }


    public void init() {
        step = 1;
        config = new HBaseConfig();
        putForm();
    }


    public HBaseConfig getConfig() {
        return config;
    }
}

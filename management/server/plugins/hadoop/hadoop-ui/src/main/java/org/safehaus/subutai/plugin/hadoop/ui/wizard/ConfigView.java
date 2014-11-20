/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.hadoop.ui.wizard;


import com.vaadin.data.Item;
import com.vaadin.ui.TreeTable;


public class ConfigView
{

    private final TreeTable configTbl;


    public ConfigView( String cfgCaption )
    {
        configTbl = new TreeTable();
        configTbl.setWidth( "100%" );
        configTbl.setHeight( "350px" );
        configTbl.addContainerProperty( cfgCaption, String.class, "" );
    }


    public void addStringCfg( String cfgCategory, String cfgValue )
    {
        Item ctg = configTbl.getItem( cfgCategory );
        if ( ctg == null )
        {
            configTbl.addItem( new Object[] { cfgCategory }, cfgCategory );
        }
        configTbl.addItem( new Object[] { cfgValue }, cfgCategory + "_" + cfgValue );
        configTbl.setParent( cfgCategory + "_" + cfgValue, cfgCategory );
        configTbl.setChildrenAllowed( cfgCategory + "_" + cfgValue, false );
        configTbl.setCollapsed( cfgCategory, false );
    }


    public TreeTable getCfgTable()
    {
        return configTbl;
    }
}

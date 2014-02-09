/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.products;

import org.safehaus.kiskismgmt.protocol.Product;

/**
 *
 * @author bahadyr
 */
public class HadoopProduct extends Product {

    private HadoopCommandEnum commands;
    private String productName = "HADOOP";

    @Override
    public HadoopCommandEnum getCommands() {
        return commands;
    }

    @Override
    public String getProductName() {
        return productName; //To change body of generated methods, choose Tools | Templates.
    }

}

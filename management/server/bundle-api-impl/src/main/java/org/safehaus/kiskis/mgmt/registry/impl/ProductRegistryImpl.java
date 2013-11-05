/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.registry.impl;

import java.util.List;
import org.safehaus.kiskis.mgmt.product.Product;
import org.safehaus.kiskis.mgmt.registry.ProductRegistry;

/**
 *
 * @author dilshat
 */
public class ProductRegistryImpl implements ProductRegistry {

    private List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> bundles) {
        this.products = bundles;
    }
}

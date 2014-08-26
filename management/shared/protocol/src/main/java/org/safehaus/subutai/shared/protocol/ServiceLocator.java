/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.shared.protocol;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author dilshat
 */
public class ServiceLocator {

	public static <T> T getService(Class<T> clazz) {
		try {
			InitialContext ctx = new InitialContext();
			String jndiName = "osgi:service/" + clazz.getName();
			return clazz.cast(ctx.lookup(jndiName));
		} catch (NamingException ex) {
			return null;
		}
	}
}

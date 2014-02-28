/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.apache.karaf.shell.console.Completer;
import java.util.List;

public class CommandCompleter implements Completer {

    public int complete(String buffer, int cursor, List candidates) {
        StringsCompleter delegate = new StringsCompleter();
        delegate.getStrings().add("option1");
        delegate.getStrings().add("option2");
        delegate.getStrings().add("etc");
        return delegate.complete(buffer, cursor, candidates);
    }
}

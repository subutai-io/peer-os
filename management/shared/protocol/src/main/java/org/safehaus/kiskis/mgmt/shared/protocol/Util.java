/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 *
 * @author dilshat
 */
public class Util {

    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isCollectionEmpty(Collection col) {
        return col == null || col.isEmpty();
    }

    public static void retainValues(Set col1, Set col2) {
        if (col1 != null && col2 != null) {
            col1.retainAll(col2);
        }
    }

    public static void removeValues(Set col1, Set col2) {
        if (col1 != null && col2 != null) {
            col1.removeAll(col2);
        }
    }

    public static boolean isFinalResponse(Response response) {
        if (response != null && response.getType() != null) {
            return response.getType() == ResponseType.EXECUTE_RESPONSE_DONE
                    || response.getType() == ResponseType.EXECUTE_TIMEOUTED;
        }
        return false;
    }

    public static String getAgentIpByMask(Agent agent, String mask) {
        if (agent != null) {
            if (agent.getListIP() != null && !agent.getListIP().isEmpty()) {
                for (String ip : agent.getListIP()) {
                    if (ip.matches(mask)) {
                        return ip;
                    }
                }
            }
            return agent.getHostname();
        }
        return null;
    }

    public static Set<Agent> filterLxcAgents(Set<Agent> agents) {
        Set<Agent> filteredAgents = new HashSet<Agent>();
        if (agents != null) {
            for (Agent agent : agents) {
                if (agent.isIsLXC()) {
                    filteredAgents.add(agent);
                }
            }
        }
        return filteredAgents;
    }

    public static String removeAllWhitespace(String str) {
        if (!isStringEmpty(str)) {
            return str.replaceAll("\\s+", "");
        } else if (str != null) {
            return "";
        }
        return null;
    }

    public static int countNumberOfOccurences(String strToSearch, String strToCount) {
        int idx = strToSearch.indexOf(strToCount);
        int count = 0;
        while (idx > -1) {
            count++;
            idx = strToSearch.indexOf(strToCount, idx + 1);
        }
        return count;
    }

    public static Set<Agent> wrapAgentToSet(Agent agent) {
        if (agent != null) {
            return new HashSet<Agent>(Arrays.asList(agent));
        }
        return new HashSet<Agent>();
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
        }

        return false;
    }

    public static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.flush();
        oos.close();
        return baos.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        ois.close();
        return o;
    }

}

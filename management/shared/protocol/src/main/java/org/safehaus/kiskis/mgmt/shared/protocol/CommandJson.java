/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandInterface;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class CommandJson {
    private static final Logger LOG = Logger.getLogger(CommandJson.class.getName());
    private static Gson gson = new Gson();

    public static Request getRequest(String json) {
        try {
            Command cmd = gson.fromJson(escape(json), Command.class);
            if (cmd.command != null) {
                return (Request) cmd.command;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequest", ex);
        }


        return null;
    }

    public static Response getResponse(String json) {
        try {
            Command cmd = gson.fromJson(escape(json), Command.class);
            if (cmd.response != null) {
                return (Response) cmd.response;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequest", ex);
        }

        return null;
    }

    public static String getJson(CommandInterface cmd) {
        try {
            return gson.toJson(cmd);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequest", ex);
        }
        return null;
    }

    public static String getAgentJson(Object agent) {
        try {
            return gson.toJson(agent);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequest", ex);
        }
        return null;
    }

    public static Agent getAgent(String json) {
        try {
            Agent agent = gson.fromJson(escape(json), Agent.class);
            if (agent != null) {
                return agent;
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getRequest", ex);
        }

        return null;
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\"");
                    break;
                case '\\':
                    sb.append("\\");
                    break;
                case '\b':
                    sb.append("\b");
                    break;
                case '\f':
                    sb.append("\f");
                    break;
                case '\n':
                    sb.append("\n");
                    break;
                case '\r':
                    sb.append("\r");
                    break;
                case '\t':
                    sb.append("\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) {
                            sb.append('0');
                        }
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
}

package org.safehaus.kiskis.mgmt.server.backend.servlet;

import org.safehaus.kiskis.mgmt.server.backend.Activator;
import org.safehaus.kiskismgmt.protocol.Agent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/30/13
 * Time: 2:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            if (request.getParameter("action") != null) {
                String action = request.getParameter("action");
                if (action.equals("getAgents")) {
                    Set<Agent> hosts = Activator.getServerBroker().getRegisteredHosts();
                    Iterator itr = hosts.iterator();
                    String result = "[";
                    while (itr.hasNext()) {
                        Agent agent = (Agent) itr.next();
                        result += "{\"agent\" : ";
                        result += "\"" + agent.getUuid() + "\"},";
                    }
                    result = result.substring(0, result.length() - 1);
                    result += "]";
                    out.write(result);
                } else if (action.equals("sendCommand")) {
                    String uuid = request.getParameter("agent").toString();
                    String command = request.getParameter("comm").toString();

                    Agent agent = new Agent();
                    agent.setUuid(uuid);
                    Activator.getServerBroker().execCommand(agent, command);
                }
            } else {
                out.write("Nothing");
            }
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

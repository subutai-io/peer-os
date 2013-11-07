package org.safehaus.kiskis.mgmt.server.broker.impl;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.safehaus.kiskismgmt.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

import org.safehaus.kiskis.mgmt.server.broker.Activator;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/10/13 Time: 4:48 PM To
 * change this template use File | Settings | File Templates.
 */
public class ResponseStorage implements RegisteredHostInterface {

    private Set<Agent> agents;
    private List<AgentOutput> agentOutputs;
    private Set<Product> products;

    public ResponseStorage() {
        agents = new HashSet<Agent>();
        agentOutputs = new CopyOnWriteArrayList<AgentOutput>();
        // TODO init set of pruducts
        Agent a1 = new Agent();
        a1.setUuid("1");
        agents.add(a1);

        Agent a2 = new Agent();
        a2.setUuid("2");
        agents.add(a2);
    }

    /**
     * Returns list of hosts
     *
     * @return
     */
    @Override
    public Set<Agent> getRegisteredHosts() {
        //System.out.println("Reading list of agents");
        //To change body of implemented methods use File | Settings | File Templates.
        return agents;
    }

    @Override
    public Set<Product> getRegisteredProducts() {
        // TODO fetch product bundles from karaf
        return products;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<AgentOutput> getAgentOutput(Agent agent) {
        // Must be thread safe to delete old data
        List<AgentOutput> output = (List<AgentOutput>) ((ArrayList) agentOutputs).clone();
        agentOutputs.clear();
        return output;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean execCommand(Agent agent, String command) {
//        Request req = new Request();
//        switch (commandEnum) {
//            case INSTALL: {
//                req.setUuid(agent.getUuid());
//                req.setProgram(product.getProductName());
//                req.setProgram("ls -l");
//            }
//        }

        Request req = CommandJson.getRequest(command);
        Command comm = new Command(req);
        Response res = Activator.getCommandSender().sendCommandToAgent(comm);
//        Command command = new Command(req);
//        Response res = Activator.getCommandSender().sendCommandToAgent(command);
        System.out.println(agent.toString());
        System.out.println(command);
//        return res != null;
        return true;
    }

    /**
     * For Communication Bundle
     *
     * @param response
     * @return
     */
    @Override
    public Request sendAgentResponse(Response response) {
        Request req = null;
        //TO-DO Create result from Response for ui
        
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                Agent agent = new Agent();
                agent.setUuid(response.getUuid());
                agents.add(agent);
                System.out.println("Agents count " + agents.size());

                req = new Request();
                req.setUuid(response.getUuid());
                req.setType(RequestType.REGISTRATION_REQUEST_DONE);
                Command command = new Command(req);

                Activator.getCommandSender().sendCommandToAgent(command);
                break;
            }
            case HEARTBEAT_RESPONSE: {
                System.out.println("HB");
                break;
            }
            case EXECUTE_RESPONSE_DONE: {
                System.out.println("ERD");
                break;
            }
            case EXECUTE_RESPONSE: {
                String stdOut = response.getStdOut();
                System.out.println(stdOut);
                break;
            }
        }

        return req;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

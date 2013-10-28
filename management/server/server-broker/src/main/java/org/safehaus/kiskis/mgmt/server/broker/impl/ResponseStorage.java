package org.safehaus.kiskis.mgmt.server.broker.impl;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.safehaus.kiskismgmt.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

import org.safehaus.kiskis.mgmt.server.broker.Activator;
import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskis.mgmt.shared.protocol.products.HadoopProduct;

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
        agentOutputs = new CopyOnWriteArrayList();
        // TODO init set of pruducts
        Agent a1 = new Agent();
        a1.setUuid("1");
        agents.add(a1);
    }
    
    /**
     * Returns list of hosts
     * @return 
     */
    @Override
    public Set<Agent> getRegisteredHosts() {
        System.out.println("Reading list of agents");
        //To change body of implemented methods use File | Settings | File Templates.
        return agents;
    }

    @Override
    public Set<Product> getRegisteredProducts() {
        // TODO fetch product bundles from karaf
        products.add(new HadoopProduct());
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
    public Boolean execCommand(Agent agent, Product product, CommandEnum commandEnum) {
        Request req = new Request();
        switch (commandEnum) {
            case INSTALL: {
                req.setUuid(agent.getUuid());
                req.setProgram(product.getProductName());
                req.setProgram("ls -l");
            }
        }

        Command command = new Command(req);
        Response res = Activator.getCommandSender().sendCommandToAgent(command);
        return res != null;
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
        System.out.println("Received response from communication: " + response.toString());

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

                System.out.println("ER");
                break;
            }
        }

        return req;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

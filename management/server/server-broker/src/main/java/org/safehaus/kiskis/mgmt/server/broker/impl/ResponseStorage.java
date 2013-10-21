package org.safehaus.kiskis.mgmt.server.broker.impl;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.safehaus.kiskismgmt.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

import org.safehaus.kiskis.mgmt.server.broker.Activator;
import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;

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
    }

    @Override
    public Set<Agent> getRegisteredHosts() {
        //To change body of implemented methods use File | Settings | File Templates.
        return agents;
    }

    @Override
    public Set<Product> getRegisteredProducts() {
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
    public Boolean execCommand(Agent agent, Product product, Enum command) {
        //Activator.getCommandSender().sendCommandToAgent(command);
        return Boolean.TRUE;  //To change body of implemented methods use File | Settings | File Templates.
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
//                Agent agent = new Agent();
//                agent.setUuid(response.getUuid());
//                agents.add(agent);
//                System.out.println("Agents count " + agents.size());
                req = new Request();

                req.setUuid(response.getUuid());
                req.setType(RequestType.REGISTRATION_REQUEST_DONE);
                System.out.println(response.getType() + " executing!!!");
                Command command = new Command(req);
//{
//  command:
//    {
//      "type":"REGISTRATION_REQUEST_DONE",
//      "uuid":"3394ef08-7b0a-4971-a428-2b241f36fe73"
//    }
//}                
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
    //private void registerAgent(Response response) {
//        response.getUuid();
    //}
}

package org.safehaus.subutai.core.communication.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.safehaus.subutai.core.communication.api.CommunicationManager;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.protocol.Request;

import javax.jms.*;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is implementation of Communication Manager.
 */
public class CommunicationManagerImpl implements CommunicationManager {

	private static final Logger LOG = Logger.getLogger(CommunicationManagerImpl.class.getName());
	/**
	 * broker
	 */
	/**
	 * pooled connection factory to hold connection to broker
	 */
	private PooledConnectionFactory pooledConnectionFactory;
	/**
	 * message listener to receive messages from broker
	 */
	private CommunicationMessageListener communicationMessageListener;
	/**
	 * executor used to send requests to agents
	 */
	private ExecutorService exec;
	/**
	 * broker URL
	 */
	private String amqUrl;
	/**
	 * service queue to listen on responses from agents
	 */
	private String amqServiceTopic;
	/**
	 * ttl of message from server to agent
	 */
	private int amqMaxMessageToAgentTtlSec;
	/**
	 * size of connection pool to broker
	 */
	private int amqMaxPooledConnections;
	/**
	 * size of executor pool to send request to agents
	 */
	private int amqMaxSenderPoolSize;

	/**
	 * Use persistent or non-persistent delivery mode for outgoing messages
	 */
	private boolean persistentMessages;


	public Connection createConnection() throws JMSException {
		return pooledConnectionFactory.createConnection();
	}


	boolean isPersistentMessages() {
		return persistentMessages;
	}


	public void setPersistentMessages(final boolean persistentMessages) {
		this.persistentMessages = persistentMessages;
	}


	int getAmqMaxMessageToAgentTtlSec() {
		return amqMaxMessageToAgentTtlSec;
	}


	public void setAmqMaxMessageToAgentTtlSec(int amqMaxMessageToAgentTtlSec) {
		this.amqMaxMessageToAgentTtlSec = amqMaxMessageToAgentTtlSec;
	}


	public void setAmqServiceTopic(String amqServiceTopic) {
		this.amqServiceTopic = amqServiceTopic;
	}


	public void setAmqMaxPooledConnections(int amqMaxPooledConnections) {
		this.amqMaxPooledConnections = amqMaxPooledConnections;
	}


	public void setAmqMaxSenderPoolSize(int amqMaxSenderPoolSize) {
		this.amqMaxSenderPoolSize = amqMaxSenderPoolSize;
	}


	public void setAmqUrl(final String amqUrl) {
		this.amqUrl = amqUrl;
	}


	/**
	 * Sends request to agent
	 *
	 * @param request - request to send
	 */
	public void sendRequest(Request request) {
		exec.submit(new CommandProducer(request, this));
	}


	@Override
	public void sendBroadcastRequest(final Request request) {
		exec.submit(new CommandProducer(request, this, true));
	}


	/**
	 * Adds listener
	 *
	 * @param listener - listener to add
	 */
	@Override
	public void addListener(ResponseListener listener) {
		try {
			if (listener != null && communicationMessageListener != null) {
				communicationMessageListener.addListener(listener);
			}
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error in addListener", ex);
		}
	}


	/**
	 * Removes listener
	 *
	 * @param listener - - listener to remove
	 */
	@Override
	public void removeListener(ResponseListener listener) {
		try {
			if (listener != null && communicationMessageListener != null) {
				communicationMessageListener.removeListener(listener);
			}
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error in removeListener", ex);
		}
	}


	public Collection getListeners() {
		return communicationMessageListener.getListeners();
	}


	/**
	 * Initializes communication manager
	 */
	public void init() {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(amqUrl), "ActiveMQ  URL is null or empty");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(amqServiceTopic), "Service queue name is null or empty");
		Preconditions.checkArgument(amqMaxMessageToAgentTtlSec >= 1,
				"Max TTL of message to agents must be greater than 0");
		Preconditions.checkArgument(amqMaxPooledConnections >= 1, "Max Pool Connections size must be greater than 0");
		Preconditions.checkArgument(amqMaxSenderPoolSize >= 1, "Max Sender Pool size must be greater than 0");

		if (pooledConnectionFactory != null) {
			try {
				pooledConnectionFactory.stop();
			} catch (Exception e) {
			}
		}

		if (communicationMessageListener != null) {
			try {
				communicationMessageListener.destroy();
			} catch (Exception e) {
			}
		}

		try {
			//executor service setup
			exec = Executors.newFixedThreadPool(amqMaxSenderPoolSize);
			//pooled connection factory setup
			ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory(amqUrl);
			amqFactory.setCheckForDuplicates(true);
			pooledConnectionFactory = new PooledConnectionFactory(amqFactory);
			pooledConnectionFactory.setMaxConnections(amqMaxPooledConnections);
			pooledConnectionFactory.start();
			setupListener();
			LOG.log(Level.INFO, "Communication Manager started...");
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error in init", ex);
		}
	}


	/**
	 * Sets up listener to receive messages from agents
	 */
	private void setupListener() {
		try {
			// Do not close this connection otherwise server listener will be closed
			Connection connection = pooledConnectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination adminQueue = session.createTopic(this.amqServiceTopic);
			MessageConsumer consumer = session.createConsumer(adminQueue);
			communicationMessageListener = new CommunicationMessageListener();
			consumer.setMessageListener(communicationMessageListener);

			Destination advisoryDestination = AdvisorySupport.getConnectionAdvisoryTopic();
			MessageConsumer advConsumer = session.createConsumer(advisoryDestination);
			advConsumer.setMessageListener(communicationMessageListener);

			// inotify topic
			Destination inotifyTopic = session.createTopic("INOTIFY_TOPIC");
			MessageConsumer inotifyConsumer = session.createConsumer(inotifyTopic);
			inotifyConsumer.setMessageListener(communicationMessageListener);
		} catch (JMSException ex) {
			LOG.log(Level.SEVERE, "Error in setupListener", ex);
		}
	}


	/**
	 * Disposes communication manager
	 */
	public void destroy() {
		try {
			if (pooledConnectionFactory != null) {
				try {
					pooledConnectionFactory.stop();
				} catch (Exception e) {
				}
			}
			if (communicationMessageListener != null) {
				try {
					communicationMessageListener.destroy();
				} catch (Exception e) {
				}
			}
			exec.shutdown();

			LOG.log(Level.INFO, "Communication Manager stopped...");
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Error in destroy", ex);
		}
	}
}

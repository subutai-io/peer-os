package org.safehaus.flume;

import org.apache.flume.node.Application;
import org.junit.rules.ExternalResource;

public class FlumeResource extends ExternalResource {

    private static int bCount = 0;
    private static int aCount = 0;
//    private Application app;
    
	@Override
	protected void before() throws Throwable {
//		app = new Application();
		final String[] args = {"--conf-file", "target/classes/flume-avro-mem-logger.properties", "--name", "agent"};
//		app.main(args);
		Application.main(args);
		Thread.sleep(1000);
//		String[] cmdArray = {"org.apache.flume.node.Application", "--conf-file", "target/classes/flume-avro-mem-logger.properties", "--name", "agent"};
//		Runtime runtime = Runtime.getRuntime();
//		runtime.exec(cmdArray);
		
/*		Thread someThread = new Thread(new Runnable() {
		    
		    public void run() {
		        Application.main(args);
		    	System.out.println("hii i have set thread as daemon");
		    }
		});
		someThread.setDaemon(true);
		someThread.start();
*/		
		System.err.println( "before test class: " + ++bCount );
		super.before();
	}
	
	@Override
	protected void after() {
		System.err.println( "after test class: " + ++aCount );
		super.after();
	}
	
}

package previousTest;

import ubuntu.JavaCheck;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 9/20/13
 * Time: 8:23 AM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class HadoopExternalResource extends ExternalResource {

    private Configuration conf;
    private String inputPath;
    private String outputPath;
    private String nameNode;
    private String jobTracker;
    private String userName;
    private String mapredJarPath;

    public HadoopExternalResource(String nameNode, String jobTracker, String userName,String inputPath, String outputPath, String mapredJarPath)
    {
        this.nameNode =  nameNode;
        this.jobTracker = jobTracker;
        this.userName = userName;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.mapredJarPath = mapredJarPath;
    }




    @Override
    public void before() throws IOException, SQLException {
       /* inputPath = "/user/root/dft";
        outputPath = "/user/root/dft-output0";
        String nameNode = "172.16.33.9";
        String jobTracker = "172.16.33.10";
        String userName = "ubuntu";*/

        System.out.println("In before method of " + this.getClass().getName());

        conf = new Configuration();
        // Check if namenode and jobtracker processes are running on the computers of the given IPs
        if (JavaCheck.checkJavaProcess("NameNode", userName + "@" + nameNode) && JavaCheck.checkJavaProcess("JobTracker",userName+"@"+jobTracker))
        {
//            System.out.println("NameNode and JobTracker are running on the given machines of IPs");
            conf.set("fs.default.name", "hdfs://" + nameNode + ":8020");
            conf.set("mapred.job.tracker", jobTracker + ":9000");
            System.setProperty("HADOOP_USER_NAME", userName);

        }
        // If namenode and jobtracker processes are NOT running on the computers of the given IPs, configure hadoop to run on the local machine
        else
        {
            System.out.println("NameNode and JobTracker are NOT running on the given machines of IPs");
            System.out.println("Switching to local machine!");
            inputPath = "/home/emin/Desktop/hadoop_play/dft";
            outputPath = "/home/emin/Desktop/hadoop_play/dft_output2";

        }
//        conf.set("mapred.jar", "/home/emin/workspace/TestLibrary/target/TestLibrary-1.0-SNAPSHOT.jar");
        conf.set("mapred.jar", mapredJarPath);

        /*JobTracker jobTracker1 = null;
        try {
            jobTracker1 = new JobTracker(conf);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        jobTracker1.setSafeMode(JobTracker.SafeModeAction.SAFEMODE_LEAVE);
        */
        FileSystem fs = FileSystem.get(conf);
        if(fs.exists(new Path(outputPath)))
        {
            System.out.println(outputPath + " exists and it is being deleted for new output!");
            fs.delete(new Path(outputPath), true);
        }
    }
    @Override
    public void after()
    {
        System.out.println("In after method of " + this.getClass().getName());
    }

    //Getters and Setters
    public String getOutputPath() {
        return outputPath;
    }
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    public Configuration getConf() {
        return conf;
    }
    public void setConf(Configuration conf) {
        this.conf = conf;
    }
    public String getInputPath() {
        return inputPath;
    }
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }
}
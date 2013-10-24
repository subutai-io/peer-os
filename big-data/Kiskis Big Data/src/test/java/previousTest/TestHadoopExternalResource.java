package previousTest;

import hadoop.WordCountExample.WordMapper;
import hadoop.WordCountExample.WordReducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 9/20/13
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class TestHadoopExternalResource {

    static String inputPath="/user/root/dft";
    static String outputPath="/user/root/dft-output0";
    static String nameNode="172.16.33.9";
    static String jobTracker="172.16.33.10";
    static String userName="ubuntu";


//    static String inputPath = "/home/emin/Desktop/hadoop_play/dft";
//    static String outputPath = "/home/emin/Desktop/hadoop_play/dft_output";
//    static String nameNode="172.16.33.8";
//    static String jobTracker="172.16.33.8";
//    static String userName="emin";

    static String mapredJarPath = "/home/emin/workspace/TestLib/target/TestLib-1.0-SNAPSHOT.jar";

    @ClassRule
    public static HadoopExternalResource hadoopResource = new HadoopExternalResource(nameNode,jobTracker,userName,inputPath,outputPath,mapredJarPath);

    @Test
    public void assingMapReduceJob() throws Exception
    {
        System.out.println("In assingMapReduceJob method of " + this.getClass().getName());
        // Assign MapReduce Job
        Job job = new Job(hadoopResource.getConf(), "wordcount");
//        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordMapper.class);
        job.setReducerClass(WordReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        FileInputFormat.addInputPath(job, new Path(hadoopResource.getInputPath()));
        FileOutputFormat.setOutputPath(job, new Path(hadoopResource.getOutputPath()));
        System.out.println("JOB JAR: " + job.getJar());
        job.waitForCompletion(true);
//        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

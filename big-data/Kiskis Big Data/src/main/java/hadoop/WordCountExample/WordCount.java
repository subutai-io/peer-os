package hadoop.WordCountExample;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 9/20/13
 * Time: 8:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class WordCount {
    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();
        String host ="172.16.33.1";
        conf.set("fs.default.name", "hdfs://" + host+ ":8020");
        conf.set("mapred.job.tracker", host + ":9000");
        conf.set("mapred.jar","/home/emin/workspace/TestLibrary/target/TestLibrary-1.0-SNAPSHOT.jar");
        System.setProperty("HADOOP_USER_NAME", "root");
        Job job = new Job(conf, "wordcount");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordMapper.class);
        job.setReducerClass(WordReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
//        FileInputFormat.addInputPath(job, new Path("/home/emin/Desktop/hadoop_play/input.txt"));
        FileInputFormat.addInputPath(job, new Path(args[1]));
        String dir =   args[2];

        /*for (int i = 1 ; i < 10 ; i ++)
        {
            if (new File(dir).exists())
            {
                System.out.println("The directory: " + dir +" exists!");
                dir = dir.substring(0, dir.length()-1) + i;
            }
            else
            {
                System.out.println("The directory: " + dir + " does not exist!");
                break;
            }
        }
*/
        FileSystem fs = FileSystem.get(conf);
        if(fs.exists(new Path(dir)))
            fs.delete(new Path(dir), true);
        FileOutputFormat.setOutputPath(job, new Path(dir));
        System.out.println("JOB JAR: " + job.getJar());
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
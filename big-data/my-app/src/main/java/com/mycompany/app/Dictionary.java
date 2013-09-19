
package com.mycompany.app;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Created with IntelliJ IDEA.
 * User: skardan
 * Date: 16-07-13
 * Time: 12:07
 */
public class Dictionary {

    public static void main(String[] args) throws Exception
    {
        Configuration conf = new Configuration();

        conf.set("fs.default.name", "hdfs://" + "localhost" + ":8020");
        conf.set("mapred.job.tracker", "localhost" + ":9000");
        conf.set("mapred.jar","/home/skardan/Desktop/my-app/target/my-app-1.0-SNAPSHOT.jar");

        Job job = new Job(conf, "dictionary");
        job.setJarByClass(Dictionary.class);
        job.setMapperClass(WordMapper.class);
        job.setReducerClass(AllTranslationsReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }
}
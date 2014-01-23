package org.safehaus.hadoop;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class HadoopMain {

	public HadoopMain() {
		// TODO Auto-generated constructor stub
	}

	  public static void main(String[] args) throws Exception {
		  
		  Configuration conf = new Configuration();
		  String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		  
//		  conf.set("fs.default.name", "hdfs://sait-lin2:8020/");
	      conf.set("mapred.child.java.opts", "-Xmx5000m");
//          conf.addResource(new Path("/opt/hadoop-1.2.1/conf/hdfs-site.xml"));
//          conf.addResource(new Path("/opt/hadoop-1.2.1/conf/mapred-site.xml"));
//          conf.addResource(new Path("/opt/hadoop-1.2.1/conf/core-site.xml"));

	        Job job = Job.getInstance(conf);
	        job.setJarByClass(UsptoMapper.class);
	        job.setJobName("uspto");
	        job.setOutputKeyClass(String.class);
	        job.setOutputValueClass(String.class);
	        
	 
	        job.setMapperClass(UsptoMapper.class); 
//	        job.setReducerClass(Reducer.class);  //Identity Reducer.
//	        job.setReducerClass(null);  //Identity Reducer.
	        job.setNumReduceTasks(0);
	 
	        job.setInputFormatClass(WholeFileInputFormat.class);
	        job.setOutputFormatClass(TextOutputFormat.class);
	 
	        FileInputFormat.setInputPaths(job, new Path("hdfs://sait-lin2/user/selcuk/2013"));
	        FileOutputFormat.setOutputPath(job, new Path("hdfs://sait-lin2/user/selcuk/2013-ooo"));
	 
	        job.waitForCompletion(true);
//	        job.submit();
	  }
}

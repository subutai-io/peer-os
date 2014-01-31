package org.safehaus.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.eclipse.jdt.core.dom.ThisExpression;

public class HadoopMain {

	public HadoopMain() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if (otherArgs.length < 2) {
			System.err.println("Usage: " + HadoopMain.class.getSimpleName() + " <input path> <output path>");
			System.exit(2);
		}

		// conf.set("fs.default.name", "hdfs://sait-lin2:8020/");
		conf.set("mapred.child.java.opts", "-Xmx3000m");

		// If configured correctly, adding path of configuration files are not
		// necessary.
		// conf.addResource(new Path("/opt/hadoop-1.2.1/conf/hdfs-site.xml"));
		// conf.addResource(new Path("/opt/hadoop-1.2.1/conf/mapred-site.xml"));
		// conf.addResource(new Path("/opt/hadoop-1.2.1/conf/core-site.xml"));

		Job job = Job.getInstance(conf);
		job.setJarByClass(UsptoMapper.class);
		job.setJobName("uspto");
		job.setOutputKeyClass(String.class);
		job.setOutputValueClass(String.class);

		job.setMapperClass(UsptoMapper.class);
		// job.setReducerClass(Reducer.class); //Identity Reducer.
		// job.setReducerClass(null); //Identity Reducer.
		job.setNumReduceTasks(0);

		job.setInputFormatClass(WholeFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		// job.submit();
		boolean result = job.waitForCompletion(true);
		// If job returns error, exit with code 1 to notify job tracker of the
		// failure
		if (!result) {
			System.exit(1);
		}
	}
}

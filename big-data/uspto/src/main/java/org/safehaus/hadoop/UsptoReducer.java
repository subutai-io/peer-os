package org.safehaus.hadoop;
import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;


public class UsptoReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
	  
	 private IntWritable totalWordCount = new IntWritable();
	  
	 @Override
	 public void reduce(Text key, Iterable<IntWritable> values, Context context)
	            throws IOException, InterruptedException {
	  int wordCount = 0;
	  Iterator<IntWritable> it=values.iterator();
	  while (it.hasNext()) {
	   wordCount += it.next().get();
	  }
	  totalWordCount.set(wordCount);
	  context.write(key, totalWordCount);
	 }
	 
}

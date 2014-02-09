package org.safehaus.hadoop;


import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class WholeFileRecordReader extends
                RecordReader<String, BytesWritable> {

        private FileSplit split;
        private Configuration conf;
        
        private final BytesWritable currValue = new BytesWritable();
        private String currKey = new String();
        private boolean fileProcessed = false;

        @Override
        public void initialize(InputSplit split, TaskAttemptContext context)
                        throws IOException, InterruptedException {
                this.split = (FileSplit)split;
                this.conf = context.getConfiguration();
        }
        
        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {        
                if ( fileProcessed ){
                        return false;
                }
                
                int fileLength = (int)split.getLength();
                byte [] result = new byte[fileLength];
                FileSystem  fs = FileSystem.get(conf);
                FSDataInputStream in = null; 
                try {
                		currKey = split.getPath().getName();
                        in = fs.open( split.getPath());
                        IOUtils.readFully(in, result, 0, fileLength);
                        currValue.set(result, 0, fileLength);
                        
                } finally {
                        IOUtils.closeStream(in);
                }
                this.fileProcessed = true;
                return true;
        }

        @Override
        public String getCurrentKey() throws IOException,
                        InterruptedException {
                return currKey;
        }

        @Override
        public BytesWritable getCurrentValue() throws IOException,
                        InterruptedException {
                return currValue;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
                // TODO Auto-generated method stub
                return 0;
        }

        @Override
        public void close() throws IOException {
                // nothing to close
        }

}

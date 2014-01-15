package examples.hadoop.WordCountExample;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 9/20/13
 * Time: 8:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class WordMapper extends Mapper<Text, Text, Text, Text> {
    private Text word = new Text();
    public void map(Text key, Text value, Context context) throws IOException, InterruptedException
    {
        StringTokenizer itr = new StringTokenizer(value.toString(),",");
        while (itr.hasMoreTokens())
        {
            word.set(itr.nextToken());
            context.write(key, word);
        }
    }
}
package hadoop.WordCountExample;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: frkn
 * Date: 9/20/13
 * Time: 8:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class WordReducer extends Reducer<Text,Text,Text,Text> {
    private Text result = new Text();
    public void reduce(Text key, Iterable<Text> values,
                       Context context
    ) throws IOException, InterruptedException
    {
        String translations = "";
        for (Text val : values)
        {
            translations += "|"+val.toString();
        }
        result.set(translations);
        context.write(key, result);
    }
}

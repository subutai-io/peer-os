package io.subutai.core.logcollector.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LogCollector implements io.subutai.core.logcollector.api.LogCollector
{
    private Queue<String> buffer;
    private AtomicInteger count;

    private int BUFFER_LIMIT = 12; // in Mb
    private String FILE_NAME = "/tmp/pending-logs";


    public void init()
    {
        buffer = new ConcurrentLinkedQueue<>();
        count = new AtomicInteger(0);
    }

    public void dispose()
    {

    }

    @Override
    public void addLogMessage( String message )
    {
        count.getAndAdd( message.length() );
        buffer.add(message);

        if( count.get() * 2 > BUFFER_LIMIT * 1000 * 1000 )
        {

            try( FileOutputStream out = new FileOutputStream(FILE_NAME, true) )
            {
                out.write(getLogsFromCollection().getBytes(StandardCharsets.UTF_8));
                out.close();
            }
            catch (FileNotFoundException e)
            {
                // @todo log file error
            }
            catch (IOException e)
            {
                // @todo log io error
            }

        }
    }

    @Override
    public String getLogMessage()
    {
        return getLogsFromCollection();
    }


    private String getLogsFromCollection()
    {
        try {
            if( Files.size(Paths.get(FILE_NAME)) > 0 )
            {
                // @todo define logic whether to read the whole file or not
            }
        }
        catch (IOException e)
        {
            // @todo log io exception
        }

        StringBuilder stringBuilder = new StringBuilder();
        while ( buffer.peek() != null )
        {
            String message = buffer.poll();
            count.getAndAdd( -message.length() );
            stringBuilder.append( message );
        }

        return stringBuilder.toString();
    }
}

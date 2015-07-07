package io.subutai.common.security.utils.io;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Class of utility methods to read from streams.
 * 
 */
public class ReadUtil
{
	private ReadUtil()
	{
	}


	/**
	 * Read all bytes from the supplied input stream. Closes the input stream.
	 * 
	 * @param is
	 *            Input stream
	 * @return All bytes
	 * @throws IOException
	 *             If an I/O problem occurs
	 */
	public static byte[] readFully( InputStream is ) throws IOException
	{
		ByteArrayOutputStream baos = null;

		try
		{
			baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[2048];
			int read = 0;

			while ( ( read = is.read( buffer ) ) != -1 )
			{
				baos.write( buffer, 0, read );
			}

			return baos.toByteArray();
		}
		finally
		{
			SafeCloseUtil.close( baos );
			SafeCloseUtil.close( is );
		}
	}
}

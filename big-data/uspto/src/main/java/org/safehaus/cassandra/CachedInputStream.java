package org.safehaus.cassandra;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedInputStream extends BufferedInputStream {

	public CachedInputStream(InputStream in) {
		super(in);
		super.mark(Integer.MAX_VALUE);
	}

	public CachedInputStream(InputStream in, int size) {
		super(in, size);
		super.mark(Integer.MAX_VALUE);
	}

	@Override
	public void close() throws IOException {
		super.reset();
	}

}

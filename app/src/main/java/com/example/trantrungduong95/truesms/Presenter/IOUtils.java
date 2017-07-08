package com.example.trantrungduong95.truesms.Presenter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class IOUtils {
	// NOTE: This class is focussed on InputStream, OutputStream, Reader and
	// Writer. Each method should take at least one of these as a parameter,
	// or return one of them.

	//The default buffer size to use.
	private static int DEFAULT_BUFFER_SIZE = 1024 * 4;

	///Instances should NOT be constructed in standard programming.

	public IOUtils() {
		super();
	}

	// copy from InputStream
	public static int copy(InputStream input, OutputStream output)
			throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}
	private static long copyLarge(InputStream input,
								  OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}

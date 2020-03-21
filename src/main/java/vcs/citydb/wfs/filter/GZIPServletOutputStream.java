package vcs.citydb.wfs.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GZIPServletOutputStream extends ServletOutputStream {
	private final ServletOutputStream servletOutputStream;
	private final GZIPOutputStream gzipOutputStream;
	
	public GZIPServletOutputStream(ServletOutputStream servletOutputStream) throws IOException {
		this.servletOutputStream = servletOutputStream;
		gzipOutputStream = new GZIPOutputStream(servletOutputStream);
	}

	@Override
	public boolean isReady() {
		return servletOutputStream.isReady();
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		servletOutputStream.setWriteListener(writeListener);
	}

	@Override
	public void write(int b) throws IOException {
		gzipOutputStream.write(b);
	}

	@Override
	public void flush() throws IOException {
		gzipOutputStream.flush();
	}

	@Override
	public void close() throws IOException {
		gzipOutputStream.close();
	}
	
	public void finish() throws IOException {
		gzipOutputStream.finish();
	}
	
}

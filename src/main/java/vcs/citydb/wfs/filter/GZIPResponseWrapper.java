package vcs.citydb.wfs.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class GZIPResponseWrapper extends HttpServletResponseWrapper {
	private GZIPServletOutputStream outputStream;
	private PrintWriter writer;

	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IllegalStateException("getWriter() has already been called for this response");

		if (outputStream == null)
			outputStream = new GZIPServletOutputStream(super.getOutputStream());

		return outputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer == null && outputStream != null)
			throw new IllegalStateException("getOutputStream() has already been called for this response");

		if (writer == null) {
			outputStream = new GZIPServletOutputStream(super.getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(outputStream, getCharacterEncoding()));
		}

		return writer;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null)
			writer.flush();
		else if (outputStream != null)
			outputStream.flush();

		super.flushBuffer();
	}

	@Override
	public void setContentLength(int len) {
		// ignore since zipped content length does not match unzipped content length
	}

	@Override
	public void setContentLengthLong(long length) {
		// ignore since zipped content length does not match unzipped content length
	}

	@Override
	public void addHeader(String name, String value) {
		if (!"content-length".equalsIgnoreCase(name))
			super.addHeader(name, value);
	}

	@Override
	public void setHeader(String name, String value) {
		if (!"content-length".equalsIgnoreCase(name))
			super.setHeader(name, value);
	}
	
	@Override
	public void addIntHeader(String name, int value) {
		if (!"content-length".equalsIgnoreCase(name))
			super.addIntHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		if (!"content-length".equalsIgnoreCase(name))
			super.setIntHeader(name, value);
	}

	public void finish() throws IOException {
		if (writer != null)
			writer.close();
		else if (outputStream != null)
			outputStream.finish();
	}

}

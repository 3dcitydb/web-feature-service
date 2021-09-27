package vcs.citydb.wfs.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GsonMessageBodyHandler<T> implements MessageBodyWriter<T>, MessageBodyReader<T> {
	private Gson gson;

	private synchronized Gson getOrCreateGson() {
		if (gson == null) {
			gson = new GsonBuilder()
					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create();
		}
		
		return gson;
	}
	
	@Override
	public long getSize(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return 0;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
	}
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return true;
	}

	@Override
	public void writeTo(T object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
	
		try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
			httpHeaders.get("Content-Type").add(MediaType.APPLICATION_JSON + "; charset=utf-8");
			getOrCreateGson().toJson(object, writer);
		}
	}
	
	@Override
	public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		
		try (InputStreamReader reader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
			Type jsonType = type.equals(genericType) ? type : genericType;
			return getOrCreateGson().fromJson(reader, jsonType);
		}
	}

}

package mdt.client;

import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import mdt.model.MDTExceptionEntity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpRESTfulClient {
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private final OkHttpClient m_client;
	protected final JsonMapper m_mapper;
	
	public HttpRESTfulClient(OkHttpClient client) {
		m_client = client;
		m_mapper = JsonMapper.builder().build();
	}
	
	public OkHttpClient getHttpClient() {
		return m_client;
	}
	
	public <T> T parseJson(String jsonStr, TypeReference<T> typeRef)
		throws JsonMappingException, JsonProcessingException {
		return m_mapper.readValue(jsonStr, typeRef);
	}
	
	public String writeJson(Object obj) throws JsonProcessingException {
		return (obj instanceof String) ? (String)obj : m_mapper.writeValueAsString(obj);
	}
	
	public RequestBody createRequestBody(Object desc) throws JsonProcessingException {
		return RequestBody.create(writeJson(desc), JSON);
	}

	public <T> T call(Request req, TypeReference<T> typeRef) {
		try {
			Response resp =  m_client.newCall(req).execute();
			return parseResponse(resp, typeRef);
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}

	public void send(Request req) {
		try {
			Response resp =  m_client.newCall(req).execute();
			if ( !resp.isSuccessful() ) {
				throwErrorResponse(resp.body().string());
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	protected String encodeBase64(String input) {
		return Base64.getEncoder().encodeToString(input.getBytes());
	}
	
	private <T> T parseResponse(Response resp, TypeReference<T> typeRef) throws MDTClientException {
		try {
			String respBody = resp.body().string();
			if ( resp.isSuccessful() ) {
				return parseJson(respBody, typeRef);
			}
			else {
				throwErrorResponse(respBody);
				throw new AssertionError();
			}
		}
		catch ( IOException e ) {
			throw new MDTClientException(resp.toString());
		}
	}
	

	private static final TypeReference<MDTExceptionEntity> EXCEPTION_ENTITY
																= new TypeReference<MDTExceptionEntity>(){};
	private void throwErrorResponse(String respBody) throws MDTClientException {
		try {
			MDTExceptionEntity entity = parseJson(respBody, EXCEPTION_ENTITY);
			throw entity.toClientException();
		}
		catch ( MDTClientException e ) {
			throw e;
		}
		catch ( Exception e ) {
			throw new InternalException("failed to parse ExceptionMessage: " + respBody);
		}
	}
}

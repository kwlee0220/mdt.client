package mdt.task;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import mdt.client.HttpRESTfulClient;
import mdt.client.MDTClientException;
import mdt.client.simulation.OperationStatus;
import mdt.client.simulation.OperationStatusResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpOperationClient {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpOperationClient.class);
	private static final TypeReference<OperationStatusResponse<String>> RESPONSE_TYPE_REF
													= new TypeReference<OperationStatusResponse<String>>(){};
	
	private final HttpRESTfulClient m_restClient;
	private final String m_endpoint;
	private Duration m_pollInterval = Duration.ofSeconds(3);
	private Duration m_timeout;
	
	public HttpOperationClient(OkHttpClient client, String endpoint) {
		m_restClient = new HttpRESTfulClient(client);
		m_endpoint = endpoint;
	}
	
	public void setPollInterval(Duration interval) {
		m_pollInterval = interval;
	}
	
	public void setTimeout(Duration timeout) {
		m_timeout = timeout;
	}
	
	public String run(String requestBody) throws CancellationException, InterruptedException {
		Instant started = Instant.now();
		OperationStatusResponse<String> resp = start(requestBody);
		
		String location = resp.getOperationLocation();
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			TimeUnit.MILLISECONDS.sleep(m_pollInterval.toMillis());
			
			resp = getStatus(location);
			if ( m_timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( m_timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					cancel(location);

					String msg = String.format("id=%s, cause=%s", location, resp.getMessage());
					throw new CancellationException(msg);
				}
			}
		}
		
		String msg;
		switch ( resp.getStatus() ) {
			case COMPLETED:
				return resp.getResult();
			case FAILED:
				msg = String.format("Server failed: id=%s, cause=%s%n", location, resp.getMessage());
				throw new MDTClientException(msg);
			case CANCELLED:
				msg = String.format("id=%s, cause=%s%n", location, resp.getMessage());
				throw new CancellationException(msg);
			default:
				throw new AssertionError();
		}
	}
	
	public OperationStatusResponse<String> start(String requestBody) {
		RequestBody body = m_restClient.createRequestBody(requestBody);
		
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending start request: url={}, body={}", m_endpoint, requestBody);
		}
		Request req = new Request.Builder().url(m_endpoint).post(body).build();
		
		try {
			Response resp =  m_restClient.getHttpClient().newCall(req).execute();
			return m_restClient.parseResponse(resp, RESPONSE_TYPE_REF);
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	public OperationStatusResponse<String> getStatus(String opId) {
		String url = String.format("%s/%s", m_endpoint, opId);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (GET) {}", url);
		}
		Request req = new Request.Builder().url(url).get().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
	
	public OperationStatusResponse<Void> cancel(String opId) {
		String url = String.format("%s/%s", m_endpoint, opId);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (DELETE) {}", url);
		}
		Request req = new Request.Builder().url(url).delete().build();
		return m_restClient.call(req, new TypeReference<OperationStatusResponse<Void>>(){});
	}
}

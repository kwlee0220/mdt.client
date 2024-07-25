package mdt.client.simulation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import mdt.client.HttpRESTfulClient;
import mdt.client.MDTClientException;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSimulationClient {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpSimulationClient.class);
	private static final TypeReference<OperationStatusResponse<Void>> RESPONSE_TYPE_REF
													= new TypeReference<OperationStatusResponse<Void>>(){};
	
	private final HttpRESTfulClient m_restClient;
	private final String m_endpoint;
	
	public HttpSimulationClient(OkHttpClient client, String endpoint) {
		m_restClient = new HttpRESTfulClient(client);
		m_endpoint = endpoint;
	}
	
	public OperationStatusResponse<Void> startSimulation(String parameters) {
		MultipartBody body = new MultipartBody.Builder()
									.setType(MultipartBody.FORM)
									.addFormDataPart("parameters", parameters)
									.build();
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending Simulation start request: url={}, body={}", m_endpoint, body);
		}
		
		Request req = new Request.Builder().url(m_endpoint).post(body).build();
		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: ({}) {}, body={}", req.method(), req.url(), parameters);
		}
		try {
			Response resp =  m_restClient.getHttpClient().newCall(req).execute();
			OperationStatusResponse<Void> statusResp = m_restClient.parseResponse(resp, RESPONSE_TYPE_REF);
			if ( statusResp.getOperationLocation() == null && resp.header("Location") != null ) {
				statusResp.setOperationLocation(resp.header("Location"));
			}
			
			return statusResp;
		}
		catch ( IOException e ) {
			throw new MDTClientException("" + e);
		}
	}
	
	public OperationStatusResponse<Void> startSimulationWithSumodelId(String submodelId) {
		String parameters = String.format("{ \"submodelId\": \"%s\"}", submodelId);
		return startSimulation(parameters);
	}
	
	public OperationStatusResponse<Void> startSimulationWithEndpoint(String submodelEndpoint) {
		String parameters = String.format("{ \"submodelEndpoint\": \"%s\"}", submodelEndpoint);
		return startSimulation(parameters);
	}
	
	public OperationStatusResponse<Void> statusSimulation(String simulationHandle) {
		String url = String.format("%s/%s", m_endpoint, simulationHandle);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (GET) {}", url);
		}
		Request req = new Request.Builder().url(url).get().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
	
	public OperationStatusResponse<Void> cancelSimulation(String simulationId) {
		String url = String.format("%s/%s", m_endpoint, simulationId);

		if ( s_logger.isDebugEnabled() ) {
			s_logger.debug("sending: (DELETE) {}", url);
		}
		Request req = new Request.Builder().url(url).delete().build();
		return m_restClient.call(req, RESPONSE_TYPE_REF);
	}
}

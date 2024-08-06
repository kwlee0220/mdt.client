package mdt.task;

import java.time.Duration;
import java.util.Map;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;

import utils.InternalException;
import utils.UnitUtils;
import utils.stream.FStream;

import mdt.client.MDTClientException;
import mdt.client.SSLUtils;
import mdt.model.AASUtils;
import okhttp3.OkHttpClient;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpBasedTask extends AbstractMDTTask {
	private static final Logger s_logger = LoggerFactory.getLogger(HttpBasedTask.class);
	private static final Duration DEFAULT_POLL_TIMEOUT = Duration.ofSeconds(3);
	private static final JsonMapper MAPPER = new JsonMapper();

	@Option(names={"--endpoint"}, paramLabel="endpoint", description="The endpoint for the HTTP-based task")
	private String m_endpoint;;

	private Duration m_pollInterval = DEFAULT_POLL_TIMEOUT;
	@Option(names={"--poll"}, paramLabel="duration", description="Status polling interval (e.g. \"5s\", \"500ms\"")
	public void setPollInterval(String intervalStr) {
		m_pollInterval = UnitUtils.parseDuration(intervalStr);
	}

	private Duration m_timeout = null;
	@Option(names={"--timeout", "-t"}, paramLabel="duration",
			description="Simulation timeout (e.g. \"30s\", \"5m\"")
	public void setTimeout(String timeoutStr) {
		m_timeout = UnitUtils.parseDuration(timeoutStr);
	}
	
	public HttpBasedTask() {
		setLogger(s_logger);
	}

	@Override
	protected void run(Map<String,Port> inputPorts, Map<String,Port> inoutPorts,
						Map<String,Port> outputPorts, Map<String,String> options) throws Exception {
		try {
			OkHttpClient http = SSLUtils.newTrustAllOkHttpClientBuilder().build();
			HttpOperationClient op = new HttpOperationClient(http, m_endpoint);
			op.setPollInterval(m_pollInterval);
			op.setTimeout(m_timeout);
			
			Map<String,Object> parameters = Maps.newHashMap();
			FStream.from(inputPorts).mapValue(Port::getAsJsonObject).forEach(parameters::put);
			FStream.from(inoutPorts).mapValue(Port::getAsJsonObject).forEach(parameters::put);
			FStream.from(options).forEach(parameters::put);
			String paramsJsonStr = AASUtils.writeJson(parameters);
			
			Map<String,Port> resultPorts = Maps.newHashMap(outputPorts);
			resultPorts.putAll(inoutPorts);
			
			String outputsJson = op.run(paramsJsonStr);
			ObjectNode top = (ObjectNode)MAPPER.readTree(outputsJson);
			FStream.from(top.fields())
					.forEach(ent -> update(resultPorts.get(ent.getKey()), ent.getValue()));
		}
		catch ( Exception e ) {
			throw new MDTClientException("Failed to create a trust-all client", e);
		}
	}
	
	private void update(Port port, JsonNode valueNode) {
		if ( valueNode.isValueNode() ) {
			try {
				String json = MAPPER.writeValueAsString(valueNode);
				port.setJson(json);
			}
			catch ( JsonProcessingException e ) {
				throw new InternalException("Failed to write JSON, cause=" + e);
			}
		}
		else {
			SubmodelElement result = AASUtils.readJson(valueNode, SubmodelElement.class);
			port.set(result);
		}
	}

	public static final void main(String... args) throws Exception {
		main(new HttpBasedTask(), args);
	}
}

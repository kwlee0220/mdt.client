package mdt.client.registry;

import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.model.registry.RegistryException;
import mdt.model.registry.SubmodelRegistry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRegistryClient extends HttpRegistryClient implements SubmodelRegistry {
	private final String m_endpoint;
	
	public HttpSubmodelRegistryClient(OkHttpClient client, String endpoint) {
		super(client);
		
		m_endpoint = endpoint;
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptors()
		throws RegistryException {
		Request req = new Request.Builder().url(m_endpoint).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public List<SubmodelDescriptor>
	getAllSubmodelDescriptorsByIdShort(String idShort) throws RegistryException {
		String url = String.format("%s?idShort=%s", m_endpoint, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public SubmodelDescriptor getSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, encodeBase64(submodelId));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelDescriptor.class);
	}

	@Override
	public SubmodelDescriptor addSubmodelDescriptor(SubmodelDescriptor desc) {
		try {
			RequestBody reqBody = createRequestBody(desc);
			
			Request req = new Request.Builder().url(m_endpoint).post(reqBody).build();
			return call(req, SubmodelDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public SubmodelDescriptor updateSubmodelDescriptorById(SubmodelDescriptor descriptor) {
		String url = String.format("%s/%s", m_endpoint, encodeBase64(descriptor.getId()));
		try {
			RequestBody reqBody = createRequestBody(descriptor);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, SubmodelDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void removeSubmodelDescriptorById(String submodelId) {
		String url = String.format("%s/%s", m_endpoint, encodeBase64(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	public void setSubmodelRepositoryEndpoint(List<String> submodelIdList, String endpoint) {
		for ( String submodelId: submodelIdList ) {
			SubmodelDescriptor desc = getSubmodelDescriptorById(submodelId);
			Endpoint ep = RegistryModelConverter.createEndpoint(endpoint, "SUBMODEL-3.0");
			desc.setEndpoints(Arrays.asList(ep));
			
			updateSubmodelDescriptorById(desc);
		}
	}
	
	@Override
	public String toString() {
		return String.format("SubmodelRegistry: endpoint=%s", m_endpoint);
	}
}

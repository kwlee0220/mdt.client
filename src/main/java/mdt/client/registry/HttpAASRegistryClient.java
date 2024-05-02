package mdt.client.registry;

import java.util.Arrays;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import mdt.model.registry.AssetAdministrationShellRegistry;
import mdt.model.registry.RegistryException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpAASRegistryClient extends HttpRegistryClient implements AssetAdministrationShellRegistry {
	private final String m_endpoint;
	
	public HttpAASRegistryClient(OkHttpClient client, String endpoint) {
		super(client);
		
		m_endpoint = endpoint;
	}

	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/%s", m_endpoint, encodeBase64(aasId));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors()
		throws RegistryException {
		Request req = new Request.Builder().url(m_endpoint).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public List<AssetAdministrationShellDescriptor>
	getAllAssetAdministrationShellDescriptorsByIdShort(String idShort) throws RegistryException {
		String url = String.format("%s?idShort=%s", m_endpoint, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, AssetAdministrationShellDescriptor.class);
	}

	@Override
	public AssetAdministrationShellDescriptor
	addAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor desc) {
		try {
			RequestBody reqBody = createRequestBody(desc);
			
			Request req = new Request.Builder().url(m_endpoint).post(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public AssetAdministrationShellDescriptor
	updateAssetAdministrationShellDescriptorById(AssetAdministrationShellDescriptor descriptor) {
		try {
			RequestBody reqBody = createRequestBody(descriptor);
			
			Request req = new Request.Builder().url(m_endpoint).put(reqBody).build();
			return call(req, AssetAdministrationShellDescriptor.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void removeAssetAdministrationShellDescriptorById(String aasId) {
		String url = String.format("%s/%s", m_endpoint, encodeBase64(aasId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	public void setAASRepositoryEndpoint(String aasId, String endpoint) {
		AssetAdministrationShellDescriptor desc = getAssetAdministrationShellDescriptorById(aasId);
		
		Endpoint ep = RegistryModelConverter.createEndpoint(endpoint, "AAS-3.0");
		desc.setEndpoints(Arrays.asList(ep));
		
		updateAssetAdministrationShellDescriptorById(desc);
	}
	
	@Override
	public String toString() {
		return String.format("AssetAdministrationShellRegistry: endpoint=%s", m_endpoint);
	}
}

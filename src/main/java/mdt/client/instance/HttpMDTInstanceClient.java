package mdt.client.instance;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.MDTClientException;
import mdt.client.repository.HttpAASRepositoryClient;
import mdt.client.repository.HttpSubmodelRepositoryClient;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstancePayload;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.instance.StartResult;
import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.repository.SubmodelRepository;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient extends HttpAASRESTfulClient implements MDTInstance {
	private final String m_endpoint;
	private final MDTInstancePayload m_payload;
	
	public HttpMDTInstanceClient(OkHttpClient client, String endpoint, MDTInstancePayload payload) {
		super(client);
		
		m_endpoint = endpoint;
		m_payload = payload;
	}

	@Override
	public String getId() {
		return m_payload.getId();
	}

	@Override
	public String getAASId() {
		return m_payload.getAasId();
	}

	@Override
	public String getAASIdShort() {
		return m_payload.getAasIdShort();
	}

	// @GetMapping("/status/{id}")
	@Override
	public MDTInstanceStatus getStatus() {
		String url = String.format("%s/status/%s", m_endpoint, getId());
		
		Request req = new Request.Builder().url(url).get().build();
		return ((MDTInstanceStatus)call(req, MDTInstanceStatus.class));
	}

    // @GetMapping("/endpoint/{id}")
	@Override
	public String getServiceEndpoint() {
		String url = String.format("%s/endpoint/%s", m_endpoint, getId());
		
		Request req = new Request.Builder().url(url).get().build();
		return (String)call(req, String.class);
	}

    // @GetMapping("/arguments/{id}")
	@Override
	public String getExecutionArguments() {
		String url = String.format("%s/arguments/%s", m_endpoint, getId());
		
		Request req = new Request.Builder().url(url).get().build();
		return (String)call(req, String.class);
	}

    // @PostMapping({"/start/{id}"})
	@Override
	public StartResult start() throws MDTInstanceManagerException {
		String url = String.format("%s/start/%s", m_endpoint, getId());

		try {
			RequestBody reqBody = createRequestBody("");
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, StartResult.class);
		}
		catch ( SerializationException e ) {
			throw new MDTClientException("" + e);
		}
	}

    // @PostMapping({"/stop/{id}"})
	@Override
	public void stop() {
		String url = String.format("%s/stop/%s", m_endpoint, getId());

		try {
			RequestBody reqBody = createRequestBody("");
			Request req = new Request.Builder().url(url).post(reqBody).build();
			send(req);
		}
		catch ( SerializationException e ) {
			throw new MDTClientException("" + e);
		}
	}

	// @GetMapping("/aas_descriptor/{id}")
	@Override
	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor() {
		String url = String.format("%s/aas_descriptor/%s", m_endpoint, getId());
		
		Request req = new Request.Builder().url(url).get().build();
		return (AssetAdministrationShellDescriptor)call(req, AssetAdministrationShellDescriptor.class);
	}

    // @GetMapping("/submodel_descriptors/{id}")
	@Override
	public List<SubmodelDescriptor> getAllSubmodelDescriptors() {
		String url = String.format("%s/submodel_descriptors/%s", m_endpoint, getId());
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}

	@Override
	public AssetAdministrationShellRepository getAssetAdministrationShellRepository() {
		String svcEp = getServiceEndpoint();
		if ( svcEp == null || svcEp.trim().length() == 0 ) {
			return null;
		}
		
		String url = String.format("%s/shells", svcEp);
		return new HttpAASRepositoryClient(getHttpClient(), url);
	}

	@Override
	public SubmodelRepository getSubmodelRepository() {
		String svcEp = getServiceEndpoint();
		if ( svcEp == null || svcEp.trim().length() == 0 ) {
			return null;
		}
		
		String url = String.format("%s/submodels", svcEp);
		return new HttpSubmodelRepositoryClient(getHttpClient(), url);
	}

	@Override
	public AssetAdministrationShellService getAssetAdministrationShellService() {
		return null;
	}

	@Override
	public List<SubmodelService> getAllSubmodelServices() {
		return null;
	}

	@Override
	public SubmodelService getSubmodelServiceById(String submodeId) {
		return null;
	}
}

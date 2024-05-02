package mdt.client.instance;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;

import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.HttpServiceFactory;
import mdt.client.Utils;
import mdt.client.registry.RegistryModelConverter;
import mdt.model.instance.AddMDTInstancePayload;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstancePayload;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.registry.AssetAdministrationShellRegistry;
import mdt.model.registry.RegistryException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.registry.ResourceNotReadyException;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceManagerClient extends HttpAASRESTfulClient implements MDTInstanceManager {
	public static final String PATH_AAS_REPOSITORY = "/registry/shell-descriptors";
	public static final String PATH_SUBMODEL_REPOSITORY = "/registry/submodel-descriptors";
	
	private final HttpServiceFactory m_serviceFactory;
	private final String m_baseUrl;
	private final String m_endpoint;
	
	public static HttpMDTInstanceManagerClient create(String host, int port) {
		try {
			HttpServiceFactory svcFact = new HttpServiceFactory();
			String endpoint = String.format("http://%s:%d/instance_manager", host, port);
			return new HttpMDTInstanceManagerClient(svcFact, endpoint);
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	public static HttpMDTInstanceManagerClient create(String endpoint) {
		try {
			HttpServiceFactory svcFact = new HttpServiceFactory();
			return new HttpMDTInstanceManagerClient(svcFact, endpoint);
		}
		catch ( Exception e ) {
			throw new MDTInstanceManagerException("" + e);
		}
	}
	
	private HttpMDTInstanceManagerClient(HttpServiceFactory svcFact, String endpoint) {
		super(svcFact.getHttpClient());
		
		m_serviceFactory = svcFact;
		
		int idx = endpoint.lastIndexOf('/');
		m_baseUrl = endpoint.substring(0, idx);
		m_endpoint = endpoint;
	}

	@Override
	public AssetAdministrationShellRegistry getAssetAdministrationShellRegistry() {
		return m_serviceFactory.getAssetAdministrationShellRegistry(m_baseUrl + PATH_AAS_REPOSITORY);
	}

	@Override
	public SubmodelRegistry getSubmodelRegistry() {
		return m_serviceFactory.getSubmodelRegistry(m_baseUrl + PATH_SUBMODEL_REPOSITORY);
	}

    // @GetMapping("/id/{id}")
	@Override
	public MDTInstance getInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/id/%s", m_endpoint, id);

		Request req = new Request.Builder().url(url).get().build();
		MDTInstancePayload payload = (MDTInstancePayload)call(req, MDTInstancePayload.class);
		return new HttpMDTInstanceClient(getHttpClient(), toMDTInstanceEndpoint(m_baseUrl), payload);
	}

    // @GetMapping("/aasId/{aasId}")
	@Override
	public MDTInstance getInstanceByAasId(String aasId) throws MDTInstanceManagerException {
		String url = String.format("%s/aasId/%s", m_endpoint, encodeBase64(aasId));
		
		Request req = new Request.Builder().url(url).get().build();
		MDTInstancePayload payload = (MDTInstancePayload)call(req, MDTInstancePayload.class);
		return new HttpMDTInstanceClient(getHttpClient(), toMDTInstanceEndpoint(m_baseUrl), payload);
	}

    // @GetMapping({"/aasIdShort/{aasIdShort}"})
	@Override
	public List<MDTInstance> getInstanceAllByIdShort(String aasIdShort) throws MDTInstanceManagerException {
		String url = String.format("%s/aasIdShort/%s", m_endpoint, aasIdShort);
		
		Request req = new Request.Builder().url(url).get().build();
		
		List<MDTInstancePayload> payloadList = callList(req, MDTInstancePayload.class);
		return FStream.from(payloadList)
						.map(p -> new HttpMDTInstanceClient(getHttpClient(),
															toMDTInstanceEndpoint(m_baseUrl), p))
						.cast(MDTInstance.class)
						.toList();
	}

    // @GetMapping({"/all"})
	@Override
	public List<MDTInstance> getInstanceAll() throws MDTInstanceManagerException {
		String url = String.format("%s/all", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		
		List<MDTInstancePayload> payloadList = callList(req, MDTInstancePayload.class);
		return FStream.from(payloadList)
						.map(p -> new HttpMDTInstanceClient(getHttpClient(),
															toMDTInstanceEndpoint(m_baseUrl), p))
						.cast(MDTInstance.class)
						.toList();
	}

    // @PutMapping({"/status"})
	@Override
	public List<MDTInstance> getAllInstancesOfStatus(MDTInstanceStatus status)
		throws MDTInstanceManagerException {
		try {
			String url = String.format("%s/status", m_endpoint);
			RequestBody reqBody = createRequestBody(status);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			
			List<MDTInstancePayload> payloadList = callList(req, MDTInstancePayload.class);
			return FStream.from(payloadList)
							.map(p -> new HttpMDTInstanceClient(getHttpClient(),
																toMDTInstanceEndpoint(m_baseUrl), p))
							.cast(MDTInstance.class)
							.toList();
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

    // @PostMapping({""})
	@Override
	public MDTInstance addInstance(String id, File aasFile, String arguments)
		throws MDTInstanceManagerException {
		try {
			// AAS Environment 정의 파일을 읽어서 AAS Registry에 등록한다.
			Environment env = Utils.readEnvironment(aasFile);
			
			AddMDTInstancePayload add = new AddMDTInstancePayload(id, env, arguments);
			RequestBody reqBody = createRequestBody(add);
			
			Request req = new Request.Builder().url(m_endpoint).post(reqBody).build();
			MDTInstancePayload payload = (MDTInstancePayload)call(req, MDTInstancePayload.class);
			
			return new HttpMDTInstanceClient(getHttpClient(), toMDTInstanceEndpoint(m_baseUrl), payload);
		}
		catch ( IOException | SerializationException e ) {
			String params = String.format("%s: (%s)", id, arguments);
			throw new MDTInstanceManagerException("failed to register an instance: "
													+ params + ", cause=" + e);
		}
	}
	
	private static final MediaType OCTET_TYPE = MediaType.parse("application/octet-stream");
	private static final MediaType JSON_TYPE = MediaType.parse("text/json");
	public MDTInstance addInstance(String id, String imageId, File jarFile, File modelFile, File confFile)
		throws MDTInstanceManagerException {
		MultipartBody.Builder builder = new MultipartBody.Builder()
											.setType(MultipartBody.FORM)
											.addFormDataPart("id", id);
		if ( imageId != null ) {
			builder = builder.addFormDataPart("imageId", imageId);
		}
		if ( jarFile != null ) {
			builder = builder.addFormDataPart("jar", "fa3st-repository.jar",
												RequestBody.create(jarFile, OCTET_TYPE));
		}
		if ( modelFile != null ) {
			builder = builder.addFormDataPart("initialModel", "model.json",
												RequestBody.create(modelFile, JSON_TYPE));
		}
		if ( confFile != null ) {
			builder = builder.addFormDataPart("instanceConf", "conf.json",
												RequestBody.create(confFile, JSON_TYPE));
		}
		RequestBody reqBody = builder.build();
		
		Request req = new Request.Builder().url(m_endpoint).post(reqBody).build();
		MDTInstancePayload payload = (MDTInstancePayload)call(req, MDTInstancePayload.class);
		
		return new HttpMDTInstanceClient(getHttpClient(), toMDTInstanceEndpoint(m_baseUrl), payload);
	}

    // @DeleteMapping("/{id}")
	@Override
	public void removeInstance(String id) throws MDTInstanceManagerException {
		String url = String.format("%s/%s", m_endpoint, id);
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

    // @DeleteMapping("")
	@Override
	public void removeInstanceAll() throws MDTInstanceManagerException {
		Request req = new Request.Builder().url(m_endpoint).delete().build();
		send(req);
	}

	@Override
	public AssetAdministrationShellService getAssetAdministrationShellService(String aasId)
		throws ResourceNotFoundException, ResourceNotReadyException, RegistryException {
		List<Endpoint> eps = getAssetAdministrationShellRegistry()
								.getAssetAdministrationShellDescriptorById(aasId)
								.getEndpoints();
		String endpoint = RegistryModelConverter.getEndpointString(eps);
		if ( endpoint == null ) {
			throw new ResourceNotReadyException("AssetAdministrationShell", aasId);
		}
		
		return m_serviceFactory.getAssetAdministrationShellService(endpoint);
	}

	@Override
	public SubmodelService getSubmodelService(String submodelId)
		throws ResourceNotFoundException, ResourceNotReadyException, RegistryException {
		List<Endpoint> eps = getSubmodelRegistry()
								.getSubmodelDescriptorById(submodelId)
								.getEndpoints();
		String endpoint = RegistryModelConverter.getEndpointString(eps);
		if ( endpoint == null ) {
			throw new ResourceNotReadyException("Submodel", submodelId);
		}
		
		return m_serviceFactory.getSubmodelService(endpoint);
	}
	
	private String toMDTInstanceEndpoint(String endpoint) {
		return m_baseUrl + "/instances";
	}
}

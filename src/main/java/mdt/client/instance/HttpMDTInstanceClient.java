package mdt.client.instance;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import utils.StateChangePoller;
import utils.Throwables;
import utils.func.Funcs;
import utils.stream.FStream;

import mdt.client.HttpAASRESTfulClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.ModelConverter;
import mdt.model.instance.InstanceDescriptor;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.instance.MDTInstanceStatus;
import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.SubmodelService;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpMDTInstanceClient extends HttpAASRESTfulClient implements MDTInstance {
	private static final RequestBody EMPTY_BODY = RequestBody.create("", null);
	
	private final HttpMDTInstanceManagerClient m_manager;
	private final String m_endpoint;
	private AtomicReference<InstanceDescriptor> m_desc;
	private final InstanceDescriptorSerDe m_serde = new InstanceDescriptorSerDe();
	
	public HttpMDTInstanceClient(HttpMDTInstanceManagerClient manager, InstanceDescriptor desc) {
		super(manager.getHttpClient());
		
		m_manager = manager;
		m_endpoint = String.format("%s/instances/%s", manager.getEndpoint(), desc.getId());
		m_desc = new AtomicReference<>(desc);
	}
	
	@Override
	public String getId() {
		return m_desc.get().getId();
	}

	@Override
	public MDTInstanceStatus getStatus() {
		return m_desc.get().getStatus();
	}

	@Override
	public String getEndpoint() {
		return m_desc.get().getEndpoint();
	}
	

	@Override
	public String getAasId() {
		return m_desc.get().getAasId();
	}

	@Override
	public String getAasIdShort() {
		return m_desc.get().getAasIdShort();
	}

	@Override
	public String getGlobalAssetId() {
		return m_desc.get().getGlobalAssetId();
	}

	@Override
	public String getAssetType() {
		return m_desc.get().getAssetType();
	}

	@Override
	public AssetKind getAssetKind() {
		return m_desc.get().getAssetKind();
	}
	


    // @GetMapping({"instances/{id}"})
	public HttpMDTInstanceClient reload() {
		InstanceDescriptor desc = m_manager.getInstanceDescriptor(getId());
		m_desc.set(desc);
		
		return this;
	}
	

    // @PutMapping({"instances/{id}/start"})
	@Override
	public void startAsync() throws MDTInstanceManagerException {
		String url = String.format("%s/start", m_endpoint);
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		InstanceDescriptor desc = m_serde.readInstanceDescriptor(descJson);
		m_desc.set(desc);
	}

	@Override
	public void start(Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException {
		startAsync();
		waitWhileStatus(MDTInstanceStatus.STARTING, pollInterval, timeout);
	}

    // @PutMapping({"instances/{id}/stop"})
	@Override
	public void stopAsync() throws MDTInstanceManagerException {
		String url = String.format("%s/stop", m_endpoint);
		Request req = new Request.Builder().url(url).put(EMPTY_BODY).build();
		
		String descJson = call(req, String.class);
		InstanceDescriptor desc = m_serde.readInstanceDescriptor(descJson);
		m_desc.set(desc);
	}

	@Override
	public void stop(Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException {
		stopAsync();
		waitWhileStatus(MDTInstanceStatus.STOPPING, pollInterval, timeout);
	}

	
	
	@Override
	public HttpAASServiceClient getAssetAdministrationShellService() {
		String endpoint = ModelConverter.toAASServiceEndpointString(getEndpoint(), getAasId());
		if ( endpoint == null ) {
			throw new InvalidResourceStatusException("AssetAdministrationShell",
													String.format("mdt=%s, id=%s", getId(), getAasId()),
													getStatus());
		}
		return new HttpAASServiceClient(getHttpClient(), endpoint);
	}

	@Override
	public List<? extends SubmodelService> getSubmodelServices() throws InvalidResourceStatusException {
		return FStream.from(getInstanceSubmodelDescriptors())
						.map(desc -> toSubmodelService(desc.getId()))
						.toList();
	}
	public SubmodelService getSubmodelServiceById(String id) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorById(id);
		return toSubmodelService(desc.getId());
	}
	public SubmodelService getSubmodelServiceByIdShort(String idShort) {
		InstanceSubmodelDescriptor desc = getInstanceSubmodelDescriptorByIdShort(idShort);
		return toSubmodelService(desc.getId());
	}

	
	public List<InstanceSubmodelDescriptor> getInstanceSubmodelDescriptors() {
		return FStream.from(m_desc.get().getSubmodels())
						.cast(InstanceSubmodelDescriptor.class)
						.toList();
	}
	

    // @GetMapping({"instances/{id}/aas_descriptor"})
	@Override
	public AssetAdministrationShellDescriptor getAASDescriptor() {
		String url = String.format("%s/aas_descriptor", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, AssetAdministrationShellDescriptor.class);
	}

    // @GetMapping({"instances/{id}/submodel_descriptors"})
	@Override
	public List<SubmodelDescriptor> getSubmodelDescriptors() {
		String url = String.format("%s/submodel_descriptors", m_endpoint);
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelDescriptor.class);
	}
	
	
	public List<String> getSubmodelIdShorts() {
		return Funcs.map(getInstanceSubmodelDescriptors(), InstanceSubmodelDescriptor::getIdShort);
	}
	
	private InstanceSubmodelDescriptor getInstanceSubmodelDescriptorById(String submodelId) {
		return FStream.from(m_desc.get().getSubmodels())
						.findFirst(d -> d.getId().equals(submodelId))
						.getOrThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	private InstanceSubmodelDescriptor getInstanceSubmodelDescriptorByIdShort(String submodelIdShort) {
		return FStream.from(m_desc.get().getSubmodels())
						.findFirst(d -> submodelIdShort.equals(d.getIdShort()))
						.getOrThrow(() -> new ResourceNotFoundException("Submodel", "idShort=" + submodelIdShort));
	}
	
	private SubmodelService toSubmodelService(String id) {
		String baseEndpoint = getEndpoint();
		if ( baseEndpoint == null ) {
			throw new InvalidResourceStatusException("MDTInstance", "id=" + getId(), getStatus());
		}
		
		String smEp = ModelConverter.toSubmodelServiceEndpointString(baseEndpoint, id);
		return new HttpSubmodelServiceClient(getHttpClient(), smEp);
	}
	
	private void waitWhileStatus(MDTInstanceStatus waitStatus, Duration pollInterval, Duration timeout)
		throws TimeoutException, InterruptedException {
		try {
			StateChangePoller.pollWhile(() -> reload().getStatus() == waitStatus)
							.interval(pollInterval)
							.timeout(timeout)
							.build()
							.run();
		}
		catch ( ExecutionException e ) {
			throw Throwables.toRuntimeException(e);
		}
	}
}

package mdt.model.instance;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import com.google.common.base.Preconditions;

import mdt.model.registry.InvalidResourceStatusException;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	public String getId();
	public MDTInstanceStatus getStatus();
	public @Nullable String getEndpoint();
	
	public String getAasId();
	public @Nullable String getAasIdShort();
	public @Nullable String getGlobalAssetId();
	public @Nullable String getAssetType();
	public @Nullable AssetKind getAssetKind();
	
	public void start(Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException;
	public void startAsync() throws MDTInstanceManagerException;
	public void stop(Duration pollInterval, @Nullable Duration timeout)
		throws MDTInstanceManagerException, TimeoutException, InterruptedException;
	public void stopAsync() throws MDTInstanceManagerException;
	
	public AssetAdministrationShellService getAssetAdministrationShellService()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	public List<? extends SubmodelService> getSubmodelServices()
		throws InvalidResourceStatusException, MDTInstanceManagerException;
	
	public AssetAdministrationShellDescriptor getAASDescriptor();
	public List<SubmodelDescriptor> getSubmodelDescriptors();
	
	public default SubmodelDescriptor getSubmodelDescriptorById(String submodelId)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelId);
		
		return getSubmodelDescriptors().stream()
								.filter(desc -> desc.getId().equals(submodelId))
								.findAny()
								.orElseThrow(() -> new ResourceNotFoundException("Submodel", "id=" + submodelId));
	}
	public default SubmodelDescriptor getSubmodelDescriptorByIdShort(String submodelIdShort)
		throws ResourceNotFoundException {
		Preconditions.checkNotNull(submodelIdShort);
		
		return getSubmodelDescriptors().stream()
								.filter(desc -> submodelIdShort.equals(desc.getIdShort()))
								.findAny()
								.orElseThrow(() -> new ResourceNotFoundException("Submodel",
																			"idShort=" + submodelIdShort));
	}
}

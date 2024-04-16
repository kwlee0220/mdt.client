package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.repository.SubmodelRepository;
import mdt.model.resource.AssetAdministrationShellService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	public String getId();
	public String getAASId();
	public @Nullable String getAASIdShort();
	
	public StatusResult start() throws MDTInstanceManagerException;
	public StatusResult stop();
	
	public MDTInstanceStatus getStatus();
	public default boolean isRunning() {
		return getStatus() == MDTInstanceStatus.RUNNING;
	}
	
	public String getServiceEndpoint();

	public AssetAdministrationShellDescriptor getAASDescriptor();
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	public AssetAdministrationShellRepository getAssetAdministrationShellRepository();
	public SubmodelRepository getSubmodelRepository();
	
	public default AssetAdministrationShellService getAssetAdministrationShellService() {
		return getAssetAdministrationShellRepository().getAssetAdministrationShellById(getAASId());
	}
}

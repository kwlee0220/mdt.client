package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;

import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.repository.SubmodelRepository;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstance {
	public String getId();
	public String getAASId();
	public @Nullable String getAASIdShort();
	
	public MDTInstanceStatus getStatus();
	public String getServiceEndpoint();
	public String getExecutionArguments();
	
	public StartResult start() throws MDTInstanceManagerException;
	public void stop();

	public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor();
	public List<SubmodelDescriptor> getAllSubmodelDescriptors();
	
	public AssetAdministrationShellRepository getAssetAdministrationShellRepository();
	public SubmodelRepository getSubmodelRepository();
	
	public default AssetAdministrationShellService getAssetAdministrationShellService() {
		return getAssetAdministrationShellRepository().getAssetAdministrationShellById(getAASId());
	}
	public default List<SubmodelService> getAllSubmodelServices() {
		return getSubmodelRepository().getAllSubmodels();
	}
	public default SubmodelService getSubmodelServiceById(String submodeId) {
		return getSubmodelRepository().getSubmodelById(submodeId);
	}
	public default List<SubmodelService> getAllSubmodelServiceByIdShort(String idShort) {
		return getSubmodelRepository().getAllSubmodelsByIdShort(idShort);
	}
}

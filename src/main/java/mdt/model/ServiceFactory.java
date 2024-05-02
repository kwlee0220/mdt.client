package mdt.model;

import mdt.model.registry.AssetAdministrationShellRegistry;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.repository.SubmodelRepository;
import mdt.model.service.AssetAdministrationShellService;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ServiceFactory {
	public AssetAdministrationShellRegistry getAssetAdministrationShellRegistry(String endpoint);
	public SubmodelRegistry getSubmodelRegistry(String endpoint);

	public AssetAdministrationShellRepository getAssetAdministrationShellRepository(String url);
	public SubmodelRepository getSubmodelRepository(String url);

	public AssetAdministrationShellService getAssetAdministrationShellService(String endpoint);
	public SubmodelService getSubmodelService(String endpoint);
}

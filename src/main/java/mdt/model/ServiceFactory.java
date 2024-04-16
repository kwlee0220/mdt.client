package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import mdt.model.registry.AssetAdministrationShellRegistry;
import mdt.model.registry.SubmodelRegistry;
import mdt.model.repository.AssetAdministrationShellRepository;
import mdt.model.repository.SubmodelRepository;
import mdt.model.resource.AssetAdministrationShellService;
import mdt.model.resource.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface ServiceFactory {
	public AssetAdministrationShellRegistry getAssetAdministrationShellRegistry(String endpoint);
	public SubmodelRegistry getSubmodelRegistry(String endpoint);

	public AssetAdministrationShellRepository getAssetAdministrationShellRepository(String url);
	public SubmodelRepository getSubmodelRepository(String url);

	public AssetAdministrationShellService getAssetAdministrationShellService(List<Endpoint> ep);
	public SubmodelService getSubmodelService(List<Endpoint> ep);
}

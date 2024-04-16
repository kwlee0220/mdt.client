package mdt.model.resource;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface AssetAdministrationShellService {
	public AssetAdministrationShell getAssetAdministrationShell();
	public AssetAdministrationShell updateAssetAdministrationShell(AssetAdministrationShell aas);

	public List<Reference> getAllSubmodelReferences();
	public Reference addSubmodelReference(Reference ref);
	public void deleteSubmodelReference(String submodelId);
	
	public AssetInformation getAssetInformation();
	public AssetInformation updateAssetInformation(AssetInformation assetInfo);
}

package mdt.model.instance;

import mdt.model.registry.AASRegistry;
import mdt.model.registry.SubmodelRegistry;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface MDTInstanceManagerClient<T extends MDTInstance> extends MDTInstanceManager<T> {
	public AASRegistry getAssetAdministrationShellRegistry();
	public SubmodelRegistry getSubmodelRegistry();
}

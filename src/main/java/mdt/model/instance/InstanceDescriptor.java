package mdt.model.instance;

import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface InstanceDescriptor {
	public String getId();
	public MDTInstanceStatus getStatus();
	@Nullable public String getEndpoint();
	
	public String getAasId();
	@Nullable public String getAasIdShort();
	@Nullable public String getGlobalAssetId();
	@Nullable public String getAssetType();
	@Nullable public AssetKind getAssetKind();
	
	public List<? extends InstanceSubmodelDescriptor> getSubmodels();
}
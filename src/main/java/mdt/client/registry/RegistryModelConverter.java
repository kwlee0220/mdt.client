package mdt.client.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;

import com.google.common.base.Preconditions;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class RegistryModelConverter {
	private static final DefaultEndpoint NULL_ENDPOINT = createEndpoint("", "UNKNOWN");
	
	private RegistryModelConverter() {
		throw new AssertionError("Should not be called: class=" + RegistryModelConverter.class);
	}
	
	public static DefaultAssetAdministrationShellDescriptor createAssetAdministrationShellDescriptor(
																				AssetAdministrationShell aas,
																				@Nullable String endpoint) {
		AssetInformation assetInfo = aas.getAssetInformation();
		DefaultEndpoint ep = (endpoint != null) ? createEndpoint(endpoint, "AAS-3.0") : NULL_ENDPOINT;
		
		DefaultAssetAdministrationShellDescriptor.Builder builder
			= new DefaultAssetAdministrationShellDescriptor.Builder()
					.administration(aas.getAdministration())
					.assetKind(aas.getAssetInformation().getAssetKind())
					.assetType(assetInfo.getAssetType())
					.globalAssetId(aas.getAssetInformation().getGlobalAssetId())
					.endpoints(Arrays.asList(ep))
					.id(aas.getId())
					.idShort(aas.getIdShort())
					.description(aas.getDescription())
					.displayName(aas.getDisplayName())
					.specificAssetIds(assetInfo.getSpecificAssetIds())
					.extensions(aas.getExtensions())
					.submodelDescriptors(Collections.emptyList());
		return builder.build();
	}
	
	public static DefaultSubmodelDescriptor createSubmodelDescriptor(Submodel submodel,
																	@Nullable String endpoint) {
		DefaultEndpoint ep = (endpoint != null) ? createEndpoint(endpoint, "SUBMODEL-3.0") : NULL_ENDPOINT;
		return new DefaultSubmodelDescriptor.Builder()
					.description(submodel.getDescription())
					.displayName(submodel.getDisplayName())
					.administration(submodel.getAdministration())
					.endpoints(Arrays.asList(ep))
					.id(submodel.getId())
					.idShort(submodel.getIdShort())
					.semanticId(submodel.getSemanticId())
					.supplementalSemanticId(submodel.getSupplementalSemanticIds())
					.build();
	}
	
	public static DefaultEndpoint createEndpoint(String endpoint, String intfc) {
		DefaultProtocolInformation protoInfo = new DefaultProtocolInformation.Builder()
													.href(endpoint)
													.endpointProtocol("HTTP")
													.endpointProtocolVersion("1.1")
													.build();
		return new DefaultEndpoint.Builder()
					._interface(intfc)
					.protocolInformation(protoInfo)
					.build();
	}
	
	public static List<Endpoint> createEndpoints(String endpoint, String intfc) {
		DefaultProtocolInformation protoInfo = new DefaultProtocolInformation.Builder()
													.href(endpoint)
													.endpointProtocol("HTTP")
													.endpointProtocolVersion("1.1")
													.build();
		Endpoint ep = new DefaultEndpoint.Builder()
							._interface(intfc)
							.protocolInformation(protoInfo)
							.build();
		return Arrays.asList(ep);
	}
	
	public static String getEndpointString(Endpoint ep) {
		Preconditions.checkNotNull(ep);
		
		String href = ep.getProtocolInformation().getHref();
		return (href != null && href.length() > 0) ? href : null;
	}
	
	public static @Nullable String getEndpointString(List<Endpoint> epList) {
		if ( epList.size() > 0 ) {
			return getEndpointString(epList.get(0));
		}
		else {
			return null;
		}
	}
}

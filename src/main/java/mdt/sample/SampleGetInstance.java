package mdt.sample;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import utils.stream.FStream;

import mdt.client.instance.HttpMDTInstanceManagerClient;
import mdt.model.AASUtils;
import mdt.model.instance.InstanceSubmodelDescriptor;
import mdt.model.instance.MDTInstance;
import mdt.model.service.AssetAdministrationShellService;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SampleGetInstance {
	private static final String ENDPOINT = "http://localhost:12985/instance-manager";
	
	public static final void main(String... args) throws Exception {
		HttpMDTInstanceManagerClient manager = HttpMDTInstanceManagerClient.connect(ENDPOINT);
		
		MDTInstance inst = manager.getInstance("내함_성형");
		System.out.printf("%-20s: %s%n", "id", inst.getId());
		System.out.printf("%-20s: %s%n", "aasId", inst.getAasId());
		System.out.printf("%-20s: %s%n", "aasIdShort", inst.getAasIdShort());
		System.out.printf("%-20s: %s%n", "status", inst.getStatus());
		System.out.printf("%-20s: %s%n", "endpoint", inst.getEndpoint());
		System.out.printf("%-20s: %s%n", "globalAssetId", inst.getGlobalAssetId());
		System.out.printf("%-20s: %s%n", "assetType", inst.getAssetType());
		System.out.printf("%-20s: %s%n", "assetKind", inst.getAssetKind());
		
		List<InstanceSubmodelDescriptor> smDescList = inst.getInstanceSubmodelDescriptors();
		for ( int i =0; i < smDescList.size(); ++i ) {
			InstanceSubmodelDescriptor d = smDescList.get(i);

			System.out.printf("%-20s: %s%n",
								String.format("submodel[%02d].id", i), d.getId());
			System.out.printf("%-20s: %s%n",
								String.format("submodel[%02d].idShort", i), d.getIdShort());
			System.out.printf("%-20s: %s%n",
								String.format("submodel[%02d].semanticId", i), d.getSemanticId());
		}
		
		System.out.println("Starting AAS Service: id=" + inst.getId());
		inst.start(Duration.ofSeconds(1), null);
		
		AssetAdministrationShellService aasSvc = inst.getAssetAdministrationShellService();
		String assetType = aasSvc.getAssetInformation().getAssetType();
		System.out.println("asset type: " + assetType);
		
		for ( Reference ref: aasSvc.getAllSubmodelReferences() ) {
			System.out.println(AASUtils.writeJson(ref));
		}
		
		inst.stop(Duration.ofSeconds(1), null);
	}
}

package mdt;

import java.time.Duration;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.async.StartableExecution;
import utils.func.KeyValue;
import utils.stream.FStream;

import mdt.client.SubmodelUtils;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.AASUtils;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class TestOperation {
	public static final void main(String... args) throws Exception {
		String id = AASUtils.encodeBase64UrlSafe("http://www.lg.co.kr/refrigerator/Innercase/Simulation/ProcessOptimization");
		String url = String.format("https://localhost:14435/api/v3.0/submodels/%s", id);
		
		HttpSubmodelServiceClient svc = HttpSubmodelServiceClient.newTrustAllSubmodelServiceClient(url);
		
		List<Property> inputValues
			= FStream.from(List.of(KeyValue.of("HT_ProcessTime", "30"), KeyValue.of("HT_FaultRate", "0.7")))
						.map(kv -> SubmodelUtils.newStringProperty(kv.key(), kv.value()))
						.toList();
//		List<SubmodelElement> outputs = svc.runOperationAsync("Operation", inputValues,
//															Duration.ofMinutes(5), Duration.ofSeconds(5));
//		System.out.println(outputs);
		StartableExecution<List<SubmodelElement>> exec
				= AASUtils.toAsyncOperation(svc, "Operation2", inputValues,
											Duration.ofMinutes(5), Duration.ofSeconds(5));
		exec.start();
		System.out.println(exec.get());
	}
}

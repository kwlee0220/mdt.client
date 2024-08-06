package mdt.task;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.client.simulation.HttpSimulationClient;
import mdt.client.simulation.OperationStatus;
import mdt.client.simulation.OperationStatusResponse;
import mdt.ksx9101.simulation.DefaultSimulationTool;
import mdt.model.instance.SubmodelReference;
import mdt.model.service.SubmodelService;
import picocli.CommandLine.Option;


/**
 * 
 * @author Kang-Woo Lee (ETRI)
 */
public class SKKUSimulationTask extends AbstractMDTTaskOld {
	private static final Logger s_logger = LoggerFactory.getLogger(SKKUSimulationTask.class);

	@Option(names={"--simulation"}, paramLabel="reference",
			description="the reference to Simulation Submodel endpoint")
	private String m_simulationSubmodelRef;
	
	public SKKUSimulationTask() {
		setLogger(s_logger);
	}

	@Override
	public void run(Map<String,String> inputValues, Map<String,Port> inputVariables,
					Map<String,Port> outputVariables) throws Exception {
		SubmodelReference simulationSubmodelRef = SubmodelReference.parseString(m_manager,
																				m_simulationSubmodelRef);
		SubmodelService simulationSubmodelSvc = simulationSubmodelRef.get();
		
		SubmodelElement sme = simulationSubmodelSvc.getSubmodelElementByPath("SimulationInfo.SimulationTool");
		DefaultSimulationTool tool = new DefaultSimulationTool();
		tool.fromAasModel((SubmodelElementCollection)sme);
		
		String legacySimulatorEndpoint = tool.getSimulatorEndpoint();
		Duration timeout = tool.getSimulationTimeout();
		
		HttpSimulationClient simulation = new HttpSimulationClient(m_manager.getHttpClient(),
																	legacySimulatorEndpoint);
		
		String submodelEndpoint = ((HttpSubmodelServiceClient)simulationSubmodelSvc).getUrl();
		
		Instant started = Instant.now();
		OperationStatusResponse<Void> resp = simulation.startSimulationWithEndpoint(submodelEndpoint);

		String targetId = simulationSubmodelRef.getInstance().getId();
		String opHandle = "ProcessOptimization";
//		String opHandle = resp.getOpHandle();
		while ( resp.getStatus() == OperationStatus.RUNNING ) {
			TimeUnit.SECONDS.sleep(3);
			
			resp = simulation.statusSimulation(opHandle);
			if ( timeout != null && resp.getStatus() == OperationStatus.RUNNING ) {
				if ( timeout.minus(Duration.between(started, Instant.now())).isNegative() ) {
					simulation.cancelSimulation(opHandle);

					System.out.printf("Simulation is cancelled: id=%s, cause=%s%n",
										targetId, resp.getMessage());
					System.exit(0);
				}
			}
		}
		
		switch ( resp.getStatus() ) {
			case COMPLETED:
				System.out.printf("Simulation completes: id=%s%n", targetId);
				System.exit(0);
			case FAILED:
				System.out.printf("Simulation is failed: id=%s, cause=%s%n", targetId, resp.getMessage());
				System.exit(-1);
			case CANCELLED:
				System.out.printf("Simulation is cancelled: id=%s, cause=%s%n", targetId, resp.getMessage());
				System.exit(-1);
			default:
				throw new AssertionError();
		}
	}

	public static final void main(String... args) throws Exception {
		main(new SKKUSimulationTask(), args);
	}
}

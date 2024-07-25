package mdt.model.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.digitaltwin.aas4j.v3.model.BaseOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;

import utils.stream.FStream;

import mdt.model.AASUtils;
import mdt.model.registry.ResourceNotFoundException;
import mdt.model.resource.value.SubmodelElementValue;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelService {
	public Submodel getSubmodel();
	public List<SubmodelElement> getAllSubmodelElements();
	public SubmodelElement getSubmodelElementByPath(String idShortPath) throws ResourceNotFoundException;
	public Submodel putSubmodel(Submodel submodel);
	public SubmodelElement postSubmodelElement(SubmodelElement element);
	public SubmodelElement postSubmodelElementByPath(String idShortPath, SubmodelElement element);
	public SubmodelElement putSubmodelElementByPath(String idShortPath, SubmodelElement element);
	public void patchSubmodelElementByPath(String idShortPath, SubmodelElement element)
		throws ResourceNotFoundException;
	public void patchSubmodelElementValueByPath(String idShortPath, SubmodelElementValue value)
		throws ResourceNotFoundException;
	public void deleteSubmodelElementByPath(String idShortPath);
	
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												javax.xml.datatype.Duration timeout);
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												javax.xml.datatype.Duration timeout);
	public OperationResult getOperationAsyncResult(OperationHandle handleId);
	public BaseOperationResult getOperationAsyncStatus(OperationHandle handleId);
	
	public default List<SubmodelElement> runOperationAsync(String operationPath,
															List<? extends SubmodelElement> inputValues,
															Duration timeout, Duration pollInterval)
		throws InterruptedException, CancellationException, TimeoutException, ExecutionException {
		List<OperationVariable> inputVariables
							= FStream.from(inputValues)
									.map(sme -> new DefaultOperationVariable.Builder().value(sme).build())
									.cast(OperationVariable.class)
									.toList();
		javax.xml.datatype.Duration jtimeout = AASUtils.DATATYPE_FACTORY.newDuration(timeout.toMillis());
		
		OperationHandle handle = invokeOperationAsync(operationPath, inputVariables, List.of(), jtimeout);
		boolean finished = false;
		while ( !finished ) {
			TimeUnit.MILLISECONDS.sleep(pollInterval.toMillis());
			finished = checkAsyncOpFinished(handle);
		}
		OperationResult result = getOperationAsyncResult(handle);
		
		return FStream.from(result.getOutputArguments())
						.map(ov -> ov.getValue())
						.toList();
	}
	
	private boolean checkAsyncOpFinished(OperationHandle handle)
		throws TimeoutException, CancellationException, ExecutionException {
		BaseOperationResult opStatus = getOperationAsyncStatus(handle);
		switch ( opStatus.getExecutionState() ) {
			case RUNNING:
			case INITIATED:
				return false;
			case COMPLETED:
				return true;
			case FAILED:
				String fullMsg = FStream.from(opStatus.getMessages())
										.join(System.lineSeparator());
				throw new ExecutionException(new Exception("Operation failed: msg=" + fullMsg));
			case TIMEOUT:
				throw new TimeoutException();
			case CANCELED:
				throw new CancellationException();
			default:
				throw new AssertionError("Unknown OperationStatus: " + opStatus.getExecutionState());
		}
	}
}

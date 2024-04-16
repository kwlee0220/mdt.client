package mdt.model.resource;

import java.util.List;

import javax.xml.datatype.Duration;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface SubmodelService {
	public Endpoint getEndpoint();
	
	public Submodel getSubmodel();
	public List<SubmodelElement> getAllSubmodelElements();
	public SubmodelElement getSubmodelElementByPath(String idShortPath);
	public Submodel updateSubmodel(Submodel submodel);
	public SubmodelElement addSubmodelElement(SubmodelElement element);
	public SubmodelElement addSubmodelElementByPath(String idShortPath, SubmodelElement element);
	public SubmodelElement updateSubmodelElementByPath(String idShortPath, SubmodelElement element);
	public void updateSubmodelElementValueByPath(String idShortPath, Object element);
	public void deleteSubmodelElementByPath(String idShortPath);
	
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments,
												Duration timeout);
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout);
	public OperationResult getOperationAsyncResult(OperationHandle handleId);
}

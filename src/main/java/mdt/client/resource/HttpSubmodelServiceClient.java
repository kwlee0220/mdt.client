package mdt.client.resource;

import java.util.List;

import javax.xml.datatype.Duration;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationHandle;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationRequest;

import mdt.client.Fa3stHttpClient;
import mdt.client.Utils;
import mdt.model.EndpointInterface;
import mdt.model.registry.RegistryException;
import mdt.model.resource.SubmodelService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelServiceClient extends Fa3stHttpClient implements SubmodelService {
	private final String m_url;
	
	public HttpSubmodelServiceClient(OkHttpClient client, String url) {
		super(client);
		
		m_url = url;
	}

	@Override
	public Endpoint getEndpoint() {
		return Utils.newEndpoint(m_url, EndpointInterface.SUBMODEL);
	}
	
	@Override
	public Submodel getSubmodel() {
		Request req = new Request.Builder().url(m_url).get().build();
		return call(req, Submodel.class);
	}
	
	@Override
	public Submodel updateSubmodel(Submodel aas) {
		try {
			RequestBody reqBody = createRequestBody(aas);
			
			Request req = new Request.Builder().url(m_url).put(reqBody).build();
			return call(req, Submodel.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public List<SubmodelElement> getAllSubmodelElements() {
		String url = m_url + "/submodel-elements";
		
		Request req = new Request.Builder().url(url).get().build();
		return callList(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement getSubmodelElementByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s", m_url, encodeBase64(idShortPath));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, SubmodelElement.class);
	}

	@Override
	public SubmodelElement addSubmodelElement(SubmodelElement element) {
		try {
			String url = m_url + "/submodel-elements";
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public SubmodelElement addSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", m_url, encodeBase64(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public SubmodelElement updateSubmodelElementByPath(String idShortPath, SubmodelElement element) {
		try {
			String url = String.format("%s/submodel-elements/%s", m_url, encodeBase64(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			return call(req, SubmodelElement.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void updateSubmodelElementValueByPath(String idShortPath, Object element) {
		try {
			String url = String.format("%s/submodel-elements/%s?content=value",
										m_url, encodeBase64(idShortPath));
			RequestBody reqBody = createRequestBody(element);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			send(req);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public void deleteSubmodelElementByPath(String idShortPath) {
		String url = String.format("%s/submodel-elements/%s", m_url, encodeBase64(idShortPath));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}

	@Override
	public OperationResult invokeOperationSync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke",
										m_url, encodeBase64(idShortPath));
			DefaultOperationRequest request = new DefaultOperationRequest.Builder()
													.inputArguments(inputArguments)
													.inoutputArguments(inoutputArguments)
													.clientTimeoutDuration(timeout)
													.build();
			RequestBody reqBody = createRequestBody(request);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, OperationResult.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public OperationHandle invokeOperationAsync(String idShortPath, List<OperationVariable> inputArguments,
												List<OperationVariable> inoutputArguments, Duration timeout) {
		try {
			String url = String.format("%s/submodel-elements/%s/invoke?async=true",
										m_url, encodeBase64(idShortPath));
			DefaultOperationRequest request = new DefaultOperationRequest.Builder()
													.inputArguments(inputArguments)
													.inoutputArguments(inoutputArguments)
													.clientTimeoutDuration(timeout)
													.build();
			RequestBody reqBody = createRequestBody(request);
			
			Request req = new Request.Builder().url(url).post(reqBody).build();
			return call(req, OperationHandle.class);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}

	@Override
	public OperationResult getOperationAsyncResult(OperationHandle handleId) {
		String url = String.format("%s/submodel-elements/%s/operation-results/%s",
									m_url, encodeBase64(handleId.getHandleId()));
		
		Request req = new Request.Builder().url(url).get().build();
		return call(req, OperationResult.class);
	}
}

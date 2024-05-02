package mdt.client.repository;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.base.Preconditions;

import utils.stream.FStream;

import mdt.client.Fa3stHttpClient;
import mdt.client.Utils;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.registry.RegistryException;
import mdt.model.repository.SubmodelRepository;
import mdt.model.service.SubmodelService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpSubmodelRepositoryClient extends Fa3stHttpClient implements SubmodelRepository {
	private final String m_url;
	
	public HttpSubmodelRepositoryClient(OkHttpClient client, String url) {
		super(client);
		
		m_url = url;
	}

	@Override
	public List<SubmodelService> getAllSubmodels() {
		Request req = new Request.Builder().url(m_url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public SubmodelService getSubmodelById(String submodelId) {
		Preconditions.checkNotNull(submodelId);
		
		String url = String.format("%s/%s", m_url, encodeBase64(submodelId));
		
		Request req = new Request.Builder().url(url).get().build();
		Submodel submodel = call(req, Submodel.class);
		return (SubmodelService)toService(submodel);
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelsByIdShort(String idShort) {
		Preconditions.checkNotNull(idShort);
		String url = String.format("%s?idShort=%s", m_url, idShort);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public List<SubmodelService> getAllSubmodelBySemanticId(String semanticId) {
		Preconditions.checkNotNull(semanticId);
		String url = String.format("%s?semanticId=%s", m_url, semanticId);
		
		Request req = new Request.Builder().url(url).get().build();
		List<Submodel> submodelList = callList(req, Submodel.class);
		return FStream.from(submodelList)
						.map(this::toService)
						.cast(SubmodelService.class)
						.toList();
	}
	
	@Override
	public HttpSubmodelServiceClient addSubmodel(Submodel submodel) {
		Preconditions.checkNotNull(submodel);
		
		try {
			RequestBody reqBody = createRequestBody(submodel);
			
			Request req = new Request.Builder().url(m_url).post(reqBody).build();
			submodel = call(req, Submodel.class);
			return toService(submodel);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}
	
	@Override
	public HttpSubmodelServiceClient updateSubmodelById(Submodel submodel) {
		Preconditions.checkNotNull(submodel);
		
		String url = String.format("%s/%s", m_url, encodeBase64(submodel.getId()));
		try {
			RequestBody reqBody = createRequestBody(submodel);
			
			Request req = new Request.Builder().url(url).put(reqBody).build();
			submodel = call(req, Submodel.class);
			return toService(submodel);
		}
		catch ( SerializationException e ) {
			throw new RegistryException("" + e);
		}
	}
	
	@Override
	public void removeSubmodelById(String submodelId) {
		Preconditions.checkNotNull(submodelId);
		
		String url = String.format("%s/%s", m_url, encodeBase64(submodelId));
		
		Request req = new Request.Builder().url(url).delete().build();
		send(req);
	}
	
	private HttpSubmodelServiceClient toService(Submodel submodel) {
		Preconditions.checkNotNull(submodel);
		
		String urlPrefix = String.format("%s/%s", m_url, Utils.encodeBase64(submodel.getId()));
		return new HttpSubmodelServiceClient(getHttpClient(), urlPrefix);
	}
}

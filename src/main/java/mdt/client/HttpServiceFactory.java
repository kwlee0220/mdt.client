package mdt.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;

import mdt.client.registry.HttpAASRegistryClient;
import mdt.client.registry.HttpSubmodelRegistryClient;
import mdt.client.registry.RegistryModelConverter;
import mdt.client.repository.HttpAASRepositoryClient;
import mdt.client.repository.HttpSubmodelRepositoryClient;
import mdt.client.resource.HttpAASServiceClient;
import mdt.client.resource.HttpSubmodelServiceClient;
import mdt.model.ServiceFactory;
import okhttp3.OkHttpClient;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class HttpServiceFactory implements ServiceFactory {
	private final OkHttpClient m_httpClient;
	
	public HttpServiceFactory() throws KeyManagementException, NoSuchAlgorithmException {
		m_httpClient = SSLUtils.newTrustAllOkHttpClientBuilder().build();
	}
	
	public OkHttpClient getHttpClient() {
		return m_httpClient;
	}
	
	@Override
	public HttpAASRegistryClient getAssetAdministrationShellRegistry(String endpoint) {
		return new HttpAASRegistryClient(m_httpClient, endpoint);
	}

	@Override
	public HttpSubmodelRegistryClient getSubmodelRegistry(String endpoint) {
		return new HttpSubmodelRegistryClient(m_httpClient, endpoint);
	}

	@Override
	public HttpAASRepositoryClient getAssetAdministrationShellRepository(String url) {
		return new HttpAASRepositoryClient(m_httpClient, url);
	}

	@Override
	public HttpSubmodelRepositoryClient getSubmodelRepository(String url) {
		return new HttpSubmodelRepositoryClient(m_httpClient, url);
	}

	@Override
	public HttpAASServiceClient getAssetAdministrationShellService(List<Endpoint> ep) {
		return new HttpAASServiceClient(m_httpClient, RegistryModelConverter.getEndpointString(ep));
	}

	@Override
	public HttpSubmodelServiceClient getSubmodelService(List<Endpoint> ep) {
		return new HttpSubmodelServiceClient(m_httpClient, RegistryModelConverter.getEndpointString(ep));
	}
}

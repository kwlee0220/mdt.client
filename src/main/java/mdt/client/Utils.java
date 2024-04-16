package mdt.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;

import com.google.common.collect.Sets;

import utils.CSV;
import utils.func.KeyValue;
import utils.stream.FStream;

import mdt.model.EndpointInterface;
import mdt.model.instance.MDTInstanceManagerException;
import mdt.model.registry.ResourceAlreadyExistsException;
import mdt.model.registry.ResourceNotFoundException;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class Utils {
	private Utils() {
		throw new AssertionError("Should not be called: class=" + Utils.class);
	}
	
	public static Map<String,String> parseParameters(String paramsString) {
		return CSV.parseCsv(paramsString, ';')
					.mapToKeyValue(part -> {
						int idx = part.indexOf('=');
						return KeyValue.of(part.substring(0, idx).trim(),
											part.substring(idx+1).trim());
					})
					.toMap();
	}

	public static String concatLangStringNameTypes(List<LangStringNameType> names) {
		if ( names != null ) {
			return FStream.from(names)
							.map(name -> name.getText())
							.join(". ");
		}
		else {
			return "";
		}
	}

	public static String concatLangStringTextTypes(List<LangStringTextType> texts) {
		if ( texts != null ) {
			return FStream.from(texts)
							.map(name -> name.getText())
							.join(". ");
		}
		else {
			return "";
		}
	}

	public static Endpoint newEndpoint(String endpoint, EndpointInterface intfc) {
		DefaultProtocolInformation protoInfo = new DefaultProtocolInformation.Builder()
													.href(endpoint)
													.endpointProtocol("HTTP")
													.endpointProtocolVersion("1.1")
													.build();
		return new DefaultEndpoint.Builder()
					._interface(intfc.getName())
					.protocolInformation(protoInfo)
					.build();
	}
	
	public static String encodeBase64(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}
	
	public static String decodeBase64(String str) {
		return new String(Base64.getDecoder().decode(str));
	}
	
	public static Environment readEnvironment(File aasEnvFile)
		throws IOException, ResourceAlreadyExistsException, ResourceNotFoundException {
		JsonDeserializer deser = new JsonDeserializer();
		
		try ( FileInputStream fis = new FileInputStream(aasEnvFile) ) {
			Environment env = deser.read(fis, Environment.class);
			if ( env.getAssetAdministrationShells().size() > 1
				|| env.getAssetAdministrationShells().size() == 0 ) {
				throw new MDTInstanceManagerException("Not supported: Multiple AAS descriptors in the Environment");
			}
			
			Set<String> submodelIds = Sets.newHashSet();
			for ( Submodel submodel: env.getSubmodels() ) {
				if ( submodelIds.contains(submodel.getId()) ) {
					throw new ResourceAlreadyExistsException("Duplicate Submodel: id=" + submodel.getId());
				}
				submodelIds.add(submodel.getId());
			}
			
			AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
			for ( Reference ref: aas.getSubmodels() ) {
				String refId = ref.getKeys().get(0).getValue();
				if ( !submodelIds.contains(refId) ) {
					throw new ResourceNotFoundException("Dangling Submodel Ref: " + refId);
				}
			}
			
			return env;
		}
		catch ( DeserializationException e ) {
			throw new MDTInstanceManagerException("failed to parse Environment: file=" + aasEnvFile);
		}
	}
}

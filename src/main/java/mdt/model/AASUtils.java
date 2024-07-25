package mdt.model;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import utils.InternalException;
import utils.async.AbstractThreadedExecution;
import utils.async.StartableExecution;

import lombok.experimental.UtilityClass;
import mdt.model.service.SubmodelService;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class AASUtils {
	public static final DatatypeFactory DATATYPE_FACTORY;
	private static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
	private static final JsonDeserializer JSON_DESERIALIZER = new JsonDeserializer();
	private static final Encoder BASE64URL_ENCODER = Base64.getUrlEncoder();
	private static final Decoder BASE64URL_DECODER = Base64.getUrlDecoder();
	
	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch ( DatatypeConfigurationException e ) {
			throw new AssertionError("" + e);
		}
	}

	public static String encodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return BASE64URL_ENCODER.encodeToString(src.getBytes(StandardCharsets.UTF_8));
		}
	}

	public static String decodeBase64UrlSafe(String src) {
		if ( Objects.isNull(src) ) {
			return null;
		}
		else {
			return new String(BASE64URL_DECODER.decode(src), StandardCharsets.UTF_8);
		}
	};
	
	public static JsonSerializer getJsonSerializer() {
		return JSON_SERIALIZER;
	}
	
	public static JsonDeserializer getJsonDeserializer() {
		return JSON_DESERIALIZER;
	}
	
	public static <T> T readJson(String json, Class<T> cls) {
		try {
			return JSON_DESERIALIZER.read(json, cls);
		}
		catch ( DeserializationException e ) {
			throw new InternalException("Failed to parse JSON: " + json);
		}
	}
	
	public static String writeJson(Object modelObj) {
		try {
			return JSON_SERIALIZER.write(modelObj);
		}
		catch ( SerializationException e ) {
			throw new InternalException("Failed to write to JSON: " + modelObj);
		}
	}
	
	public static String toJson(SubmodelElement sme) {
		try {
			return JSON_SERIALIZER.write(sme);
		}
		catch ( SerializationException e ) {
			throw new InternalException("Failed to serialize SubmodelElement: " + sme);
		}
	}
	
	public static StartableExecution<List<SubmodelElement>>
	toAsyncOperation(SubmodelService svc, String opIdShortPath, List<? extends SubmodelElement> inputValues,
					Duration timeout, Duration pollInterval) {
		return new AbstractThreadedExecution<List<SubmodelElement>>() {
			@Override
			protected List<SubmodelElement> executeWork()
					throws InterruptedException, CancellationException, Exception {
				return svc.runOperationAsync(opIdShortPath, inputValues, timeout, pollInterval);
			}
		};
	}
	
	public static class NULLClass { }
}

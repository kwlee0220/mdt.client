package mdt.model;

import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;

import utils.CSV;
import utils.stream.FStream;

import lombok.experimental.UtilityClass;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
@UtilityClass
public class ReferenceUtils {
	public static Reference toAASReference(String id) {
		Key key = new DefaultKey.Builder()
								.value(id)
								.type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
								.build();
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(key)
									.build();
	}
	
	public static Reference toSubmodelReference(String id) {
		Key key = new DefaultKey.Builder()
								.value(id)
								.type(KeyTypes.SUBMODEL)
								.build();
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(key)
									.build();
	}

	public static Reference toSubmodelElementReference(String submodelId, List<String> idShortPath) {
		Key smKey = new DefaultKey.Builder()
									.type(KeyTypes.SUBMODEL)
									.value(submodelId)
									.build();
		List<Key> keyList = FStream.from(idShortPath)
									.map(id -> ReferenceUtils.newKey(KeyTypes.SUBMODEL_ELEMENT, id))
									.toList();
		keyList.add(0, smKey);
		
		return new DefaultReference.Builder()
									.type(ReferenceTypes.MODEL_REFERENCE)
									.keys(keyList)
									.build();
	}

	public static Reference toSubmodelElementReference(String submodelId, String idShortPath) {
		return toSubmodelElementReference(submodelId, CSV.parseCsv(idShortPath, '.').toList());
	}

	private static Key newKey(KeyTypes keyType, String idShort) {
		return new DefaultKey.Builder()
								.type(keyType)
								.value(idShort)
								.build();
	}
}

package mdt.client;

import javax.annotation.Nullable;

import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.func.Lazy;
import utils.stream.FStream;

import lombok.experimental.Delegate;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class EquipmentParameter implements DataElement {
	@Delegate
	private final SubmodelElementCollection m_smeColl;
	
	private final Lazy<Property> m_id = Lazy.of(() -> findProperty("ParameterName"));
	private final Lazy<Property> m_name = Lazy.of(() -> findProperty("ParameterName"));
	private final Lazy<Property> m_type = Lazy.of(() -> findProperty("ParameterType"));
	private final Lazy<Property> m_grade = Lazy.of(() -> findProperty("ParameterGrade"));
	private final Lazy<Property> m_uomCode = Lazy.of(() -> findProperty("ParameterUOMCode"));
	private final Lazy<Property> m_opId = Lazy.of(() -> findProperty("OperationId"));
	private final Lazy<Property> m_lsl = Lazy.of(() -> findProperty("LSL"));
	private final Lazy<Property> m_pdci = Lazy.of(() -> findProperty("PeriodicDataCollectionIndicator"));
	private final Lazy<Property> m_dcPeriod = Lazy.of(() -> findProperty("DataCollectionPeriod"));
	private final Lazy<Property> m_eventDatetime = Lazy.of(() -> findProperty("EventDateTime"));
	private final Lazy<Property> m_value = Lazy.of(() -> findProperty("ParameterValue"));
	private final Lazy<Property> m_validCode = Lazy.of(() -> findProperty("ValidationResultCode"));
	
	public static boolean isEquipmentParameter(SubmodelElement sme) {
		if ( sme instanceof SubmodelElementCollection smec ) {
			String cat = smec.getCategory();
			return ( cat != null ) ? cat.trim().equalsIgnoreCase("PARAMETER") : false;
		}
		else {
			return false;
		}
	}
	
	public static @Nullable EquipmentParameter safeCastFrom(SubmodelElement sme) {
		if ( isEquipmentParameter(sme) ) {
			return new EquipmentParameter((SubmodelElementCollection)sme);
		}
		else {
			return null;
		}
	}
	
	private EquipmentParameter(SubmodelElementCollection sme) {
		m_smeColl = sme;
	}
	
	public String getId() {
		return m_id.get().getValue();
	}
	
	public String getName() {
		return m_name.get().getValue();
	}
	
	public String getType() {
		return m_type.get().getValue();
	}
	
	public String getValue() {
		return m_value.get().getValue();
	}
	
	private Property findProperty(String idShort) {
		return FStream.from(m_smeColl.getValue())
						.castSafely(Property.class)
						.findFirst(sme -> sme.getIdShort().equals(idShort))
						.getOrNull();
	}
}

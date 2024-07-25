package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface EquipmentParameter extends Parameter {
	public String getEquipmentId();
	public void setEquipmentId(String equipmentId);
	
	public default String getEntityId() {
		return getEquipmentId();
	}
	public default void setEntityId(String containerId) {
		setEquipmentId(containerId);
	}
}

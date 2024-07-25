package mdt.ksx9101.model;

/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface OperationParameter extends Parameter {
	public String getOperationId();
	public void setOperationId(String equipmentId);
	
	public default String getEntityId() {
		return getOperationId();
	}
	public default void setEntityId(String containerId) {
		setOperationId(containerId);
	}
}

package mdt.model;

import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public interface DataType<T> {
	public String getId();
	public DataTypeDefXsd getTypeDefXsd();
	public Class<T> getJavaClass();
	
	public default String getName() {
		return getTypeDefXsd().name();
	}
	
	public String toValueString(T value);
	public T parseValueString(String str);
}

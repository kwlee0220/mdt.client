package mdt.cli.tree;

import java.util.function.Predicate;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.stream.FStream;

import mdt.client.EquipmentParameter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public class SubmodelElementNodeFactory {
	public static Node toNode(SubmodelElement smElm, Predicate<SubmodelElement> filter) {
		if ( filter != null && !filter.test(smElm) ) {
			return null;
		}
		
		if ( smElm instanceof Property p ) {
			boolean isParameter = FStream.of(p.getCategory().split(";"))
											.map(String::trim)
											.exists(cat -> cat.equalsIgnoreCase("PARAMETER"));
			if ( isParameter ) {
				String value = p.getValue().trim();
				return (value == null || value.length() == 0) ? null : new PropertyNode(p);
			}
		}
		else if ( smElm instanceof SubmodelElementCollection sec) {
			EquipmentParameter param = EquipmentParameter.safeCastFrom(sec);
			if ( param != null ) {
				return new EquipmentParameterNode(param, filter);
			}
			else {
				return new SubmodelElementCollectionNode(sec, filter);
			}
		}
		else if ( smElm instanceof SubmodelElementList smel) {
			return new SubmodelElementListNode(smel, filter);
		}

		return null;
	}
}

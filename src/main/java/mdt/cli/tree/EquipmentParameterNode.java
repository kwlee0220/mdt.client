package mdt.cli.tree;

import java.util.Collections;
import java.util.function.Predicate;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import mdt.client.EquipmentParameter;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class EquipmentParameterNode implements Node {
	private EquipmentParameter m_param;
	private Predicate<SubmodelElement> m_filter;
	
	public EquipmentParameterNode(EquipmentParameter param, Predicate<SubmodelElement> filter) {
		m_param = param;
		m_filter = filter;
	}

	@Override
	public String getText() {
		if ( m_param.getType() != null ) {
			return String.format("%s (%s): %s", m_param.getId(), m_param.getType(), m_param.getValue());
		}
		else {
			return String.format("%s: %s", m_param.getId(), m_param.getValue());
		}
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}

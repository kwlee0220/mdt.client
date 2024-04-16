package mdt.cli.tree;

import java.util.Collections;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class PropertyNode implements Node {
	private Property m_prop;
	
	public PropertyNode(Property prop) {
		m_prop = prop;
	}

	@Override
	public String getText() {
		return String.format("%s=%s", m_prop.getIdShort(), m_prop.getValue());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}

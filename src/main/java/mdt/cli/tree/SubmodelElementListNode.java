package mdt.cli.tree;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelElementListNode implements Node {
	private SubmodelElementList m_list;
	
	public SubmodelElementListNode(SubmodelElementList smel) {
		m_list = smel;
	}

	@Override
	public String getText() {
		return String.format("%s", m_list.getIdShort());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_list.getValue())
						.map(sme -> SubmodelElementNodeFactory.toNode(sme));
	}
}

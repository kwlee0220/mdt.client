package mdt.cli.tree;

import java.util.function.Predicate;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelElementCollectionNode implements Node {
	private SubmodelElementCollection m_coll;
	private Predicate<SubmodelElement> m_filter;
	
	public SubmodelElementCollectionNode(SubmodelElementCollection prop, Predicate<SubmodelElement> filter) {
		m_coll = prop;
		m_filter = filter;
	}

	@Override
	public String getText() {
		return String.format("%s", m_coll.getIdShort());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return FStream.from(m_coll.getValue())
						.flatMapNullable(sme -> SubmodelElementNodeFactory.toNode(sme, m_filter));
	}
}

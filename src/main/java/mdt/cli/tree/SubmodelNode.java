package mdt.cli.tree;

import java.util.List;

import org.barfuin.texttree.api.Node;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;

import com.google.common.collect.Lists;

import utils.stream.FStream;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class SubmodelNode implements Node {
	private Submodel m_submodel;
	
	public SubmodelNode(Submodel submodel) {
		m_submodel = submodel;
	}

	@Override
	public String getText() {
		String idShortStr = "";
		if ( m_submodel.getIdShort() != null ) {
			idShortStr = String.format(", (%s)", m_submodel.getIdShort());
		}
		return String.format("%s%s", m_submodel.getId(), idShortStr);
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = Lists.newArrayList();
		
		if ( m_submodel.getSemanticId() != null ) {
			String id = m_submodel.getSemanticId().getKeys().get(0).getValue();
			children.add(new TextNode(String.format("semanticId: %s", id)));
		}
		
		FStream.from(m_submodel.getSubmodelElements())
				.map(sme -> SubmodelElementNodeFactory.toNode("", sme))
				.forEach(children::add);
		
		return children;
	}
}

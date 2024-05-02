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
		return String.format("%s", m_submodel.getIdShort());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> attributes = Lists.newArrayList();
		attributes.add(new TextNode("ID: " + m_submodel.getId()));
		if ( !isEmpty(m_submodel.getIdShort()) ) {
			attributes.add(new TextNode("ID_SHORT: " + m_submodel.getIdShort()));
		}
		
		return FStream.from(attributes)
					.concatWith(FStream.from(m_submodel.getSubmodelElements())
							.map(sme -> SubmodelElementNodeFactory.toNode(sme, null)));
	}
	
	private boolean isEmpty(Object obj) {
		return (obj == null || obj.toString().trim().length() == 0);
	}
}

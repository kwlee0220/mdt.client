package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.Routing;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class RoutingNode implements Node {
	private Routing m_routing;
	
	public RoutingNode(Routing routing) {
		m_routing = routing;
	}

	@Override
	public String getText() {
		return String.format("Routing: %s, Item=%s setup=%s", m_routing.getRoutingName(), m_routing.getItemID(),
													m_routing.getSetupTime());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
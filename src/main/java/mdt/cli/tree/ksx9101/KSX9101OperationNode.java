package mdt.cli.tree.ksx9101;

import java.util.List;

import org.barfuin.texttree.api.Node;

import utils.func.Funcs;
import utils.stream.FStream;

import mdt.cli.tree.ListNode;
import mdt.ksx9101.model.Operation;
import mdt.ksx9101.model.ParametricEntity;
import mdt.ksx9101.model.ProductionOrder;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class KSX9101OperationNode extends ParametricEntityNode {
	private final Operation m_operation;
	
	public KSX9101OperationNode(Operation operation) {
		m_operation = operation;
	}

	@Override
	protected ParametricEntity getParametricEntity() {
		return m_operation;
	}

	@Override
	public String getText() {
		String nameStr = Funcs.toNonNull(m_operation.getOperationName(), m_operation.getOperationId());
		return String.format("Operation (%s)", nameStr);
	}
	
	@Override
	public Iterable<? extends Node> getChildren() {
		List<Node> children = FStream.from(super.getChildren()).cast(Node.class).toList();
		ListNode<ProductionOrder> orders = new ListNode<>("ProductionOrders",
														m_operation.getProductionOrders(),
														ProductionOrderNode::new);
		children.add(orders);
		
		return children;
	}
}

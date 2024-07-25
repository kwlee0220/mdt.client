package mdt.cli.tree.ksx9101;

import java.util.Collections;

import org.barfuin.texttree.api.Node;

import mdt.ksx9101.model.ItemMaster;


/**
 *
 * @author Kang-Woo Lee (ETRI)
 */
public final class ItemMasterNode implements Node {
	private ItemMaster m_itemMaster;
	
	public ItemMasterNode(ItemMaster itemMaster) {
		m_itemMaster = itemMaster;
	}

	@Override
	public String getText() {
		return String.format("Item: %s (%s)", m_itemMaster.getItemName(), m_itemMaster.getItemType());
	}

	@Override
	public Iterable<? extends Node> getChildren() {
		return Collections.emptyList();
	}
}
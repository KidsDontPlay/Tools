package mrriegel.flexibletools.item;

import mrriegel.flexibletools.handler.CTab;

public class ItemPick extends GenericItemTool implements ITool {

	public ItemPick() {
		super("picki", "pickaxe");
		setCreativeTab(CTab.TAB);
	}

}

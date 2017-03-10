package mrriegel.tools.item;

import mrriegel.tools.handler.CTab;

public class ItemPick extends GenericItemTool implements ITool {

	public ItemPick() {
		super("picki",  "pickaxe");
		setCreativeTab(CTab.TAB);
	}

}

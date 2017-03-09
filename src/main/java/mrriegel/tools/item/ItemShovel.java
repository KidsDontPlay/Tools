package mrriegel.tools.item;

import mrriegel.tools.handler.CTab;

public class ItemShovel extends GenericItemTool implements ITool {

	public ItemShovel() {
		super("shovi",  "shovel");
		setCreativeTab(CTab.TAB);
	}

}

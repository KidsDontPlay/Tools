package mrriegel.flexibletools.item;

import mrriegel.flexibletools.handler.CTab;

public class ItemShovel extends GenericItemTool implements ITool {

	public ItemShovel() {
		super("shovi", "shovel");
		setCreativeTab(CTab.TAB);
	}

}

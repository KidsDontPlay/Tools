package mrriegel.flexibletools.item;

import mrriegel.flexibletools.handler.CTab;

public class ItemAxpickvel extends GenericItemTool implements ITool {

	public ItemAxpickvel() {
		super("multi", "pickaxe", "axe", "shovel");
		setCreativeTab(CTab.TAB);
	}

}

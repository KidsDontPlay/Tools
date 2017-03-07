package mrriegel.tools.item;

import mrriegel.tools.ToolHelper;
import mrriegel.tools.handler.CTab;

public class ItemAxpickvel extends GenericItemTool implements ITool {

	public ItemAxpickvel() {
		super("multi",  "pickaxe", "axe", "shovel");
		setCreativeTab(CTab.TAB);
		setHarvestLevel("pickaxe", toolMaterial.getHarvestLevel());
	}

}

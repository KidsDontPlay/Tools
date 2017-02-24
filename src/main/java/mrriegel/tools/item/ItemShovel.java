package mrriegel.tools.item;

import mrriegel.tools.ToolHelper;
import mrriegel.tools.handler.CTab;

public class ItemShovel extends GenericItemTool implements ITool {

	public ItemShovel() {
		super("shovi", ToolHelper.fin, "shovel");
		setCreativeTab(CTab.TAB);
		setHarvestLevel("pickaxe", toolMaterial.getHarvestLevel());
	}

}

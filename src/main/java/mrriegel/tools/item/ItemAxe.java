package mrriegel.tools.item;

import mrriegel.tools.ToolHelper;
import mrriegel.tools.handler.CTab;

public class ItemAxe extends GenericItemTool implements ITool {

	public ItemAxe() {
		super("axi", ToolHelper.fin, "axe");
		setCreativeTab(CTab.TAB);
		setHarvestLevel("pickaxe", toolMaterial.getHarvestLevel());
	}

}

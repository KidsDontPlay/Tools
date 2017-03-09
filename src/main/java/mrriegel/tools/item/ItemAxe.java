package mrriegel.tools.item;

import mrriegel.tools.ToolHelper;
import mrriegel.tools.handler.CTab;

public class ItemAxe extends GenericItemTool implements ITool {

	public ItemAxe() {
		super("axi", "axe");
		setCreativeTab(CTab.TAB);
	}

}

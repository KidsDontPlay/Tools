package mrriegel.tools.handler;

import mrriegel.tools.Tools;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CTab {

	public static final CreativeTabs TAB = new CreativeTabs(Tools.MODID) {

		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(Items.CLAY_BALL);
		}

		public String getTranslatedTabLabel() {
			return Tools.MODNAME;
		};
	};

}

package mrriegel.flexibletools.handler;

import mrriegel.flexibletools.FlexibleTools;
import mrriegel.flexibletools.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CTab {

	public static final CreativeTabs TAB = new CreativeTabs(FlexibleTools.MODID) {

		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(ModItems.pick);
		}

		@Override
		public String getTranslatedTabLabel() {
			return FlexibleTools.MODNAME;
		};
	};

}

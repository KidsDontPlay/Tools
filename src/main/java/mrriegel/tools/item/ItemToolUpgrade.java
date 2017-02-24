package mrriegel.tools.item;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.ArrayUtils;

import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.tools.handler.CTab;

import com.google.common.collect.Lists;

public class ItemToolUpgrade extends CommonSubtypeItem {

	public enum Upgrade {
		ExE("area", .8f, tools(true)), //
		SxS("area", .4f, tools(true)), //
		VEIN("area", .8f, tools(false)), //
		AUTOMINE("area", .3f, "pickaxe"), //
		MAGNET("transport", tools(true)), //
		TELE("transport", tools(true)), //
		POISON("effect", tools(false)), //
		FIRE("effect", tools(true)), //
		SLOW("effect", "sword"), //
		WITHER("effect", "sword"), //
		DAMAGE1("support", tools(true)), //
		DAMAGE2("support", tools(true)), //
		SPEED("support", 2.2f, tools(true)), //
		LUCK("support", tools(true)), //
		SILK("support", tools(false)), //
		XP("support", tools(true)), //
		HEAL("after", tools(true)), //
		REPAIR("support", tools(true));

		public String category;
		public List<String> toolClasses;
		float speedMultiplier;

		Upgrade(String category, float speedMultiplier, String... toolClasses) {
			this.category = category;
			this.toolClasses = Lists.newArrayList(toolClasses);
			this.speedMultiplier = speedMultiplier;
		}

		Upgrade(String category, String... toolClasses) {
			this(category, 1f, toolClasses);
		}

		private static String[] tools(boolean sword) {
			String[] ar = new String[] { "pickaxe", "axe", "shovel" };
			if (sword)
				return ArrayUtils.add(ar, "sword");
			else
				return ar;
		}
	}

	public ItemToolUpgrade() {
		super("tool_upgrade", Upgrade.values().length);
		setCreativeTab(CTab.TAB);
		setMaxStackSize(1);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (!"".isEmpty())
			return super.getUnlocalizedName(stack);
		return Upgrade.values()[stack.getItemDamage()].name();
	}

}

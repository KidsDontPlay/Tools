package mrriegel.tools.item;

import java.util.List;
import java.util.stream.Collectors;

import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.tools.handler.CTab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ItemToolUpgrade extends CommonSubtypeItem {

	public enum Upgrade {
		ExE("area", .7f, 1, tools(true)), //
		SxS("area", .4f, 1, tools(true)), //
		VEIN("area", .3f, 1, tools(false)), //
		AUTOMINE("area", .2f, 1, "pickaxe"), //
		MAGNET("transport", 1, tools(true)), //
		TELE("transport", 1, tools(true)), //
		POISON("effect", 1, tools(true)), //
		FIRE("effect", 1, tools(true)), //
		SLOW("effect", 1, tools(true)), //
		WITHER("effect", 1, tools(true)), //
		HEAL("effect", 1, tools(true)), //
		DAMAGE("support", 4, tools(true)), //
		SPEED("support", 2.2f, 4, tools(false)), //
		LUCK("support", 3, tools(true)), //
		SILK("support", 1, tools(false)), //
		XP("support", 3, "sword"), //
		REPAIR("support", 1, tools(true)), //
		TORCH("skill", 1, tools(true)), //
		PORT("skill", 1, tools(true)), //
		BAG("skill", 1, tools(true));

		public final String category;
		public List<String> toolClasses;
		public final float speedMultiplier;
		public final int max;

		Upgrade(String category, float speedMultiplier, int max, String... toolClasses) {
			this.category = category;
			this.toolClasses = Lists.newArrayList(toolClasses);
			this.speedMultiplier = speedMultiplier;
			this.max = max;
		}

		Upgrade(String category, int max, String... toolClasses) {
			this(category, 1f, max, toolClasses);
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

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add("Tools: " + Joiner.on(", ").join(Upgrade.values()[stack.getItemDamage()].toolClasses.stream().map(WordUtils::capitalize).collect(Collectors.toList())));
	}

}

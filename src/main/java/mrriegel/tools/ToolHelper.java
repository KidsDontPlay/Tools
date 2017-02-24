package mrriegel.tools;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.tools.item.ITool;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

public class ToolHelper {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	public static Set<Upgrade> getUpgrades(ItemStack stack) {
		if (!(stack.getItem() instanceof ITool))
			return Collections.EMPTY_SET;
		return NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : Upgrade.values()[s.getItemDamage()]).filter(u -> u != null).collect(Collectors.toSet());
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).contains(upgrade);
	}

}

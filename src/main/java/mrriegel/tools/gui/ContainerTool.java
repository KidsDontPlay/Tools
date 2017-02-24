package mrriegel.tools.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.gui.slot.SlotFilter;
import mrriegel.tools.item.ItemToolUpgrade;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContainerTool extends CommonContainerItem {

	private static Map<Integer, Predicate<ItemStack>> valids = Maps.newHashMap();
	static {
		valids.put(0, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && Upgrade.values()[s.getItemDamage()].category.equals("area") && Collections.disjoint(Upgrade.values()[s.getItemDamage()].toolClasses, s.getItem().getToolClasses(s))));
		valids.put(1, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && Upgrade.values()[s.getItemDamage()].category.equals("transport")&& Collections.disjoint(Upgrade.values()[s.getItemDamage()].toolClasses, s.getItem().getToolClasses(s))));
	}

	public ContainerTool(InventoryPlayer invPlayer) {
		super(invPlayer, 15);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.inventory.getCurrentItem() == stack;
	}

	@Override
	protected void initSlots() {
		//area
		initSlots(getItemInventory(), 20, 12, 1, 1, 0, SlotFilter.class, valids.get(0));
		//transport
		initSlots(getItemInventory(), 40, 12, 1, 1, 1, SlotFilter.class, valids.get(1));
		initPlayerSlots(8, 84);
	}

	@Override
	protected List<Area> allowedSlots(ItemStack stack, IInventory inv, int index) {
		if (stack.getItem() instanceof ItemToolUpgrade) {
			if (inv == invPlayer) {
				List<Area> ars = Lists.newArrayList();
				for (int i : valids.keySet())
					if (valids.get(i).test(stack))
						ars.add(getAreaForInv(getItemInventory(), i, 1));
				return ars;
			} else if (inv == getItemInventory())
				return Lists.newArrayList(getAreaForEntireInv(invPlayer));
		}
		return null;
	}
}

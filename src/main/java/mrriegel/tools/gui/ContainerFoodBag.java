package mrriegel.tools.gui;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.gui.slot.SlotFilter;

public class ContainerFoodBag extends CommonContainerItem {

	public ContainerFoodBag(InventoryPlayer invPlayer, int num) {
		super(invPlayer, num);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.inventory.getCurrentItem() == stack;
	}

	@Override
	protected void initSlots() {
		Predicate<ItemStack> p = ((ItemStack s) -> s.getItem() instanceof ItemFood);
		initSlots(getItemInventory(), 80, 12, 5, 3, 0, SlotFilter.class, p);
		initPlayerSlots(8, 84);
	}

	@Override
	protected List<Area> allowedSlots(ItemStack stack, IInventory inv, int index) {
		if (stack.getItem() instanceof ItemFood) {
			if (inv == invPlayer)
				return Lists.newArrayList(getAreaForEntireInv(getItemInventory()));
			else if (inv == getItemInventory())
				return Lists.newArrayList(getAreaForEntireInv(invPlayer));
		}
		return null;
	}

}

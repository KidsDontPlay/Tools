package mrriegel.tools.gui;

import java.util.List;
import java.util.function.Predicate;

import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.gui.slot.SlotFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import com.google.common.collect.Lists;

public class ContainerFoodBag extends CommonContainerItem {

	EnumHand hand;

	public ContainerFoodBag(InventoryPlayer invPlayer, EnumHand hand) {
		super(invPlayer, 15);
		this.hand = hand;
		stack = invPlayer.player.getHeldItem(hand);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getHeldItem(hand) == stack;

	}

	@Override
	protected void initSlots() {
		initSlots(getItemInventory(), 80, 12, 5, 3, 0, SlotFilter.class, (Predicate<ItemStack>) ((ItemStack s) -> s.getItem() instanceof ItemFood));
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

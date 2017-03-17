package mrriegel.flexibletools.gui;

import java.util.List;
import java.util.Objects;

import mrriegel.flexibletools.item.ITool;
import mrriegel.flexibletools.item.ItemToolUpgrade;
import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.helper.NBTStackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import com.google.common.collect.Lists;

public class ContainerBag extends CommonContainerItem {

	EnumHand hand;
	int slot = -1, toolHash;

	public ContainerBag(InventoryPlayer invPlayer, EnumHand hand, boolean shift) {
		super(invPlayer, 27);
		this.hand = hand;
		toolHash = Objects.hashCode(getPlayer().getHeldItem(hand));
		List<ItemStack> lis = NBTStackHelper.getItemStackList(getPlayer().getHeldItem(hand), "items");
		slot = !shift ? 7 : 8;
		if (lis.size() >= 9) {
			stack = lis.get(slot);
		} else
			stack = ItemStack.EMPTY;
		readFromStack();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return stack != null && stack.getItem() instanceof ItemToolUpgrade && playerIn.getHeldItem(hand).getItem() instanceof ITool && Objects.hashCode(playerIn.getHeldItem(hand)) == toolHash;
	}

	@Override
	public void writeToStack() {
		super.writeToStack();
		if (slot != -1) {
			List<ItemStack> lis = NBTStackHelper.getItemStackList(getPlayer().getHeldItem(hand), "items");
			lis.set(slot, stack);
			NBTStackHelper.setItemStackList(getPlayer().getHeldItem(hand), "items", lis);
		}
	}

	@Override
	protected void initSlots() {
		initSlots(getItemInventory(), 8, 13, 9, 3);
		initPlayerSlots(8, 74);
	}

	@Override
	protected List<Area> allowedSlots(ItemStack stack, IInventory inv, int index) {
		if (inv == invPlayer)
			return Lists.newArrayList(getAreaForEntireInv(getItemInventory()));
		else if (inv == getItemInventory())
			return Lists.newArrayList(getAreaForEntireInv(invPlayer));
		return null;
	}

}

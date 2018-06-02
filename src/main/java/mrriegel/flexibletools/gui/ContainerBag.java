package mrriegel.flexibletools.gui;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import mrriegel.flexibletools.item.ITool;
import mrriegel.flexibletools.item.ItemToolUpgrade;
import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.helper.NBTStackHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ContainerBag extends CommonContainerItem {

	int slot = -1, toolHash;

	public ContainerBag(InventoryPlayer invPlayer, boolean shift) {
		super(invPlayer, 27);
		toolHash = Objects.hashCode(getPlayer().getHeldItemMainhand());
		List<ItemStack> lis = NBTStackHelper.getList(getPlayer().getHeldItemMainhand(), "items", ItemStack.class);
		slot = !shift ? 7 : 8;
		if (lis.size() >= 9) {
			stack = lis.get(slot);
		} else
			stack = ItemStack.EMPTY;
		readFromStack();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return stack != null && stack.getItem() instanceof ItemToolUpgrade && playerIn.getHeldItemMainhand().getItem() instanceof ITool && Objects.hashCode(playerIn.getHeldItemMainhand()) == toolHash;
	}

	@Override
	public void writeToStack() {
		super.writeToStack();
		if (slot != -1) {
			List<ItemStack> lis = NBTStackHelper.getList(getPlayer().getHeldItemMainhand(), "items", ItemStack.class);
			lis.set(slot, stack);
			NBTStackHelper.setList(getPlayer().getHeldItemMainhand(), "items", lis);
		}
	}

	@Override
	protected void initSlots() {
		initSlots(getItemInventory(), 8, 13, 9, 3);
		initPlayerSlots(8, 74);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (slotId >= 0 && slotId < inventorySlots.size() && inventorySlots.get(slotId).inventory instanceof InventoryPlayer && inventorySlots.get(slotId).getSlotIndex() == invPlayer.currentItem)
			return ItemStack.EMPTY;
		return super.slotClick(slotId, dragType, clickTypeIn, player);
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

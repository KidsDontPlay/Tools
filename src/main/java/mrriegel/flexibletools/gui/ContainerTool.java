package mrriegel.flexibletools.gui;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import mrriegel.flexibletools.item.ItemToolUpgrade;
import mrriegel.limelib.gui.CommonContainerItem;
import mrriegel.limelib.gui.slot.SlotFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContainerTool extends CommonContainerItem {

	private Map<Integer, Predicate<ItemStack>> valids;
	private EnumHand hand;

	public ContainerTool(InventoryPlayer invPlayer, EnumHand hand) {
		super(invPlayer, 9);
		this.hand = hand;
		stack = invPlayer.player.getHeldItem(hand);
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return playerIn.getHeldItem(hand) == stack;
	}

	@Override
	protected void modifyInvs() {
		valids = Maps.newHashMap();
		valids.put(0, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && ItemToolUpgrade.getUpgrade(s).category.equals("area") && ItemToolUpgrade.getUpgrade(s).isValid(stack)));
		valids.put(1, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && ItemToolUpgrade.getUpgrade(s).category.equals("transport") && ItemToolUpgrade.getUpgrade(s).isValid(stack)));
		valids.put(2, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && ItemToolUpgrade.getUpgrade(s).category.equals("effect") && ItemToolUpgrade.getUpgrade(s).isValid(stack)));
		for (int i = 3; i < 7; i++)
			valids.put(i, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && ItemToolUpgrade.getUpgrade(s).category.equals("support") && ItemToolUpgrade.getUpgrade(s).isValid(stack)));
		for (int i = 7; i < 9; i++)
			valids.put(i, ((ItemStack s) -> s.getItem() instanceof ItemToolUpgrade && ItemToolUpgrade.getUpgrade(s).category.equals("skill") && ItemToolUpgrade.getUpgrade(s).isValid(stack)));
		invs.put("inv", new InventoryBasic("null", false, invs.get("inv").getSizeInventory()) {
			@Override
			public int getInventoryStackLimit() {
				return 1;
			}
		});
		super.modifyInvs();
	}

	@Override
	protected void initSlots() {
		initSlots(getItemInventory(), 8, 18, 1, 1, 0, SlotFilter.class, valids.get(0));
		initSlots(getItemInventory(), 28, 18, 1, 1, 1, SlotFilter.class, valids.get(1));
		initSlots(getItemInventory(), 48, 18, 1, 1, 2, SlotFilter.class, valids.get(2));
		initSlots(getItemInventory(), 68, 18, 2, 2, 3, SlotFilter.class, valids.get(3));
		initSlots(getItemInventory(), 108, 18, 1, 1, 7, SlotFilter.class, valids.get(7));
		initSlots(getItemInventory(), 128, 18, 1, 1, 8, SlotFilter.class, valids.get(7));
		initPlayerSlots(8, 84 - 16);
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (hand == EnumHand.MAIN_HAND && slotId >= 0 && slotId < inventorySlots.size() && inventorySlots.get(slotId).inventory instanceof InventoryPlayer && inventorySlots.get(slotId).getSlotIndex() == invPlayer.currentItem)
			return ItemStack.EMPTY;
		ItemStack s = super.slotClick(slotId, dragType, clickTypeIn, player);
		return s;
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

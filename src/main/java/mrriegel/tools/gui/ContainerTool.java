package mrriegel.tools.gui;

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
import net.minecraft.util.EnumHand;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ContainerTool extends CommonContainerItem {

	private Map<Integer, Predicate<ItemStack>> valids;
	private EnumHand hand;

	public ContainerTool(InventoryPlayer invPlayer, EnumHand hand) {
		super(invPlayer, 15);
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
		valids.put(0, ((ItemStack s) -> Upgrade.values()[s.getItemDamage()].category.equals("area") && Upgrade.values()[s.getItemDamage()].toolClasses.stream().anyMatch(stack.getItem().getToolClasses(stack)::contains)));
		valids.put(1, ((ItemStack s) -> Upgrade.values()[s.getItemDamage()].category.equals("transport") && Upgrade.values()[s.getItemDamage()].toolClasses.stream().anyMatch(stack.getItem().getToolClasses(stack)::contains)));
		valids.put(2, ((ItemStack s) -> Upgrade.values()[s.getItemDamage()].category.equals("effect") && Upgrade.values()[s.getItemDamage()].toolClasses.stream().anyMatch(stack.getItem().getToolClasses(stack)::contains)));
		for (int i = 3; i < 7; i++)
			valids.put(i, ((ItemStack s) -> Upgrade.values()[s.getItemDamage()].category.equals("support") && Upgrade.values()[s.getItemDamage()].toolClasses.stream().anyMatch(stack.getItem().getToolClasses(stack)::contains)));
		super.modifyInvs();
	}

	@Override
	protected void initSlots() {
		initSlots(getItemInventory(), 8, 12, 1, 1, 0, SlotFilter.class, valids.get(0));
		initSlots(getItemInventory(), 28, 12, 1, 1, 1, SlotFilter.class, valids.get(1));
		initSlots(getItemInventory(), 48, 12, 1, 1, 2, SlotFilter.class, valids.get(2));
		initSlots(getItemInventory(), 68, 12, 2, 2, 3, SlotFilter.class, valids.get(3));
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

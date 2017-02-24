package mrriegel.tools.item;

import java.awt.Color;
import java.util.List;

import mrriegel.limelib.LimeLib;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.item.CommonItem;
import mrriegel.tools.Tools;
import mrriegel.tools.gui.ContainerFoodBag;
import mrriegel.tools.handler.CTab;
import mrriegel.tools.handler.GuiHandler.ID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

public class ItemFoodBag extends CommonItem {

	public ItemFoodBag() {
		super("food_bag");
		setCreativeTab(CTab.TAB);
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		playerIn.openGui(Tools.instance, ID.FOODBAG.ordinal(), worldIn, handIn.ordinal(), 0, 0);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (worldIn.isRemote || (worldIn.getTotalWorldTime() + itemSlot) % 20 != 0)
			return;
		EntityPlayer player = (EntityPlayer) entityIn;
		if (!player.isCreative() && player.getFoodStats().needFood() && !(player.openContainer instanceof ContainerFoodBag)) {
			List<ItemStack> lis = NBTStackHelper.getItemStackList(stack, "items");
			List<ItemStack> drop = Lists.newArrayList();
			for (ItemStack s : lis) {
				if (s.getItem() instanceof ItemFood) {
					s.getItem().onItemUseFinish(s, worldIn, player);
					break;
				} else {
					drop.add(s.copy());
					s = ItemStack.EMPTY;
				}
			}
			for (ItemStack s : drop)
				player.dropItem(s, false);
			NBTStackHelper.setItemStackList(stack, "items", lis);
		}
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.hasTagCompound() && (LimeLib.proxy.getClientWorld().getTotalWorldTime() / 10) % 2 == 0 && NBTStackHelper.getItemStackList(stack, "items").stream().allMatch(s -> s.isEmpty());
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 0D;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return Color.RED.getRGB();
	}

}

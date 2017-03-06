package mrriegel.tools.item;

import mrriegel.limelib.item.CommonItem;
import mrriegel.tools.handler.CTab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class ItemLifter extends CommonItem {

	public ItemLifter() {
		super("lifter");
		setCreativeTab(CTab.TAB);
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (!worldIn.isRemote) {
			for (int i = (int)(playerIn.posY) + 1; i < worldIn.getHeight(); i++) {
				MutableBlockPos pos = new MutableBlockPos(new BlockPos(playerIn));
				pos.setY(i);
//				IBlockState state = worldIn.getBlockState(pos);
				if (worldIn.isBlockFullCube(pos)) {
					if (worldIn.isAirBlock(pos.up()) && worldIn.isAirBlock(pos.up(2))) {
						playerIn.setPositionAndUpdate(playerIn.posX, pos.getY() + 1.05, playerIn.posZ);
						return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
					}
				}
			}
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

}

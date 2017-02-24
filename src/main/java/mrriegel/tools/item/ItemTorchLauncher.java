package mrriegel.tools.item;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.datapart.DataPartWorker;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.item.CommonItem;
import mrriegel.tools.handler.CTab;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ItemTorchLauncher extends CommonItem {

	public ItemTorchLauncher() {
		super("torch_launcher");
		setCreativeTab(CTab.TAB);
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
		if (!worldIn.isRemote) {
			final ItemStack TORCH = new ItemStack(Blocks.TORCH);
			ItemStack torch = InvHelper.extractItem(new PlayerMainInvWrapper(player.inventory), (ItemStack s) -> s.getItem() == Item.getItemFromBlock(Blocks.TORCH), 1, true);
			RayTraceResult ray = ForgeHooks.rayTraceEyes(player, 30);
			if (ray != null && ray.typeOfHit == Type.BLOCK && ray.sideHit != EnumFacing.DOWN) {
				BlockPos pos = ray.getBlockPos();
				IBlockState iblockstate = worldIn.getBlockState(pos);
				EnumFacing facing = ray.sideHit;
				Block block = iblockstate.getBlock();
				if (!block.isReplaceable(worldIn, pos)) {
					pos = pos.offset(facing);
				}
				if (player.canPlayerEdit(pos, facing, TORCH) && worldIn.mayPlace(Blocks.TORCH, pos, false, facing, (Entity) null)) {
					int i = this.getMetadata(TORCH.getMetadata());
					IBlockState iblockstate1 = Blocks.TORCH.getStateForPlacement(worldIn, pos, facing, 0, 0, 0, i, player, EnumHand.MAIN_HAND);
					worldIn.setBlockState(pos, iblockstate1);
					torch = InvHelper.extractItem(new PlayerMainInvWrapper(player.inventory), (ItemStack s) -> s.getItem() == Item.getItemFromBlock(Blocks.TORCH), 1, false);
					if (torch.isEmpty() && !player.isCreative()) {
						TorchPart part = new TorchPart();
						DataPartRegistry reg = DataPartRegistry.get(worldIn);
						BlockPos p = reg.nextPos(pos);
						if (p == null)
							worldIn.setBlockToAir(pos);
						else {
							part.torch = pos;
							reg.addDataPart(p, part, false);
						}
					}
					return ActionResult.<ItemStack> newResult(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
				} else {
					return ActionResult.<ItemStack> newResult(EnumActionResult.FAIL, player.getHeldItem(handIn));
				}
			}
		}
		return super.onItemRightClick(worldIn, player, handIn);
	}

	public static class TorchPart extends DataPartWorker {
		BlockPos torch;
		int ticks = 0;
		boolean done = false;

		@Override
		protected boolean workDone(World world, Side side) {
			return done;
		}

		@Override
		protected boolean canWork(World world, Side side) {
			return true;
		}

		@Override
		protected void work(World world, Side side) {
			ticks++;
			if (ticks >= 100) {
				done = true;
				if (world.getBlockState(torch).getBlock() == Blocks.TORCH)
					world.setBlockToAir(torch);
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound compound) {
			torch = BlockPos.fromLong(NBTHelper.getLong(compound, "torch"));
			ticks = NBTHelper.getInt(compound, "ticks");
			done = NBTHelper.getBoolean(compound, "done");
			super.readFromNBT(compound);
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			NBTHelper.setLong(compound, "torch", torch.toLong());
			NBTHelper.setInt(compound, "ticks", ticks);
			NBTHelper.setBoolean(compound, "done", done);
			return super.writeToNBT(compound);
		}
	}

}

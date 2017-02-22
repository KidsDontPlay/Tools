package mrriegel.tools.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.datapart.DataPartWorker;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.WorldHelper;
import mrriegel.limelib.util.Utils;
import mrriegel.tools.handler.CTab;

public class ItemPick extends GenericItemTool {

	public ItemPick() {
		super(fin, "pickaxe");
		setRegistryName("picki");
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CTab.TAB);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
		if (!worldIn.isRemote) {
			DataPartRegistry.get(worldIn).addDataPart(pos, new Miner(), true);
		}
		return true;
	}

	public static class Miner extends DataPartWorker {

		@Override
		protected boolean workDone(World world, Side side) {
			return lis!=null&&lis.isEmpty();
		}

		@Override
		protected boolean canWork(World world, Side side) {
			return side.isServer();
		}

		List<BlockPos> lis;

		@Override
		protected void work(World world, Side side) {
			if (side.isClient())
				return;
			if (lis == null) {
				lis = WorldHelper.getCuboid(pos, 2);
			}
			else {
				BlockPos p = lis.remove(0);
				while (world.isAirBlock(p) && !lis.isEmpty())
					p = lis.remove(0);
				for (ItemStack s : BlockHelper.breakBlockWithFortune(world, p, 0, Utils.getFakePlayer((WorldServer) world), false, true))
					Block.spawnAsEntity(world, pos, s);
			}
		}

	}

}

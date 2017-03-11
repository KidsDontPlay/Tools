package mrriegel.tools.block;

import java.util.List;
import java.util.Random;

import mrriegel.limelib.block.CommonBlock;
import mrriegel.tools.handler.CTab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

public class BlockGlowSand extends CommonBlock {

	public BlockGlowSand() {
		super(Material.SAND, "glow_sand");
		setCreativeTab(CTab.TAB);
		setLightLevel(.8F);
		setHardness(.4F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.875D, 1.0D);
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
		double velocity = Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
		double velo = 0.4;
		if (!(entity instanceof EntityPlayerSP) || velocity == 0 || velocity >= velo)
			return;
		EntityPlayerSP player = (EntityPlayerSP) entity;
		if (Math.abs(player.movementInput.moveForward) < 0.75f && Math.abs(player.movementInput.moveStrafe) < 0.75f)
			return;
		entity.motionX = velo * entity.motionX / velocity;
		entity.motionZ = velo * entity.motionZ / velocity;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		// TODO Auto-generated method stub
		return Lists.newArrayList(new ItemStack(Items.GUNPOWDER, new Random().nextInt(3) + 1));
	}

}

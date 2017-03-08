package mrriegel.tools;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import mrriegel.limelib.util.Utils;
import mrriegel.tools.handler.GuiHandler.ID;
import mrriegel.tools.item.ItemToolUpgrade;
import mrriegel.tools.item.ItemToolUpgrade.TorchPart;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import mrriegel.tools.network.MessageParticle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.google.common.collect.Lists;

public class ToolHelper {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	public static List<Upgrade> getUpgrades(ItemStack stack) {
		return NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : ((ItemToolUpgrade) s.getItem()).getUpgrade(s)).filter(u -> u != null).collect(Collectors.toList());
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).contains(upgrade);
	}

	public static int getUpgradeCount(ItemStack stack, Upgrade upgrade) {
		return (int) getUpgrades(stack).stream().filter(u -> u == upgrade).count();
	}

	public static void breakBlocks(ItemStack tool, EntityPlayer player, BlockPos orig, List<BlockPos> posses) {
		if (player.world.isRemote)
			return;
		NonNullList<ItemStack> drops = NonNullList.<ItemStack> create();
		float origHard = player.world.getBlockState(orig).getBlockHardness(player.world, orig);
		boolean radius = isUpgrade(tool, Upgrade.ExE) || isUpgrade(tool, Upgrade.SxS);
		for (BlockPos pos : posses) {
			if (radius && origHard + 25F < player.world.getBlockState(pos).getBlockHardness(player.world, pos))
				continue;
			if (!tool.isEmpty() && !player.world.isAirBlock(pos) && (pos.equals(orig) || (BlockHelper.isToolEffective(tool, player.world, pos, false) && BlockHelper.canToolHarvestBlock(player.world, pos, tool)))) {
				NonNullList<ItemStack> tmp = BlockHelper.breakBlock(player.world, pos, player.world.getBlockState(pos), player, isUpgrade(tool, Upgrade.SILK), getUpgradeCount(tool, Upgrade.LUCK), true, true);
				for (ItemStack s : tmp)
					StackHelper.addStack(drops, s);
				if (player.world.isAirBlock(pos)) {
					damageItem(1, player, tool);
				}
			}
		}
		if (!drops.isEmpty()) {
			boolean fire = smeltItems(player, drops);
			handleItems(player, orig, drops, posses);
			if (fire)
				for (BlockPos p : posses)
					PacketHandler.sendTo(new MessageParticle(p, MessageParticle.SMELT), (EntityPlayerMP) player);
		}
	}

	public static void breakBlock(ItemStack tool, EntityPlayer player, BlockPos orig, BlockPos pos) {
		breakBlocks(tool, player, orig, Collections.singletonList(pos));
	}

	public static void damageItem(int damage, EntityPlayer player, ItemStack tool) {
		if (tool.getItemDamage() == tool.getMaxDamage()) {
			for (ItemStack s : NBTStackHelper.getItemStackList(tool, "items")) {
				if (!s.isEmpty())
					player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY + .3, player.posZ, s));
			}
		}
		if (!player.isCreative())
			tool.damageItem(1, player);
	}

	private static void handleItems(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks, Iterable<BlockPos> posses) {
		ItemStack tool = player.getHeldItemMainhand();
		if (ToolHelper.isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, orig, stacks, posses);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			NonNullList<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks)
				set.add(ItemHandlerHelper.insertItem(inv, s.copy(), false));
			PacketHandler.sendTo(new MessageParticle(orig, MessageParticle.TELE), (EntityPlayerMP) player);
			handleItemsDefault(player, orig, set, posses);
		} else
			handleItemsDefault(player, orig, stacks, posses);
	}

	private static boolean smeltItems(EntityPlayer player, NonNullList<ItemStack> stacks) {
		boolean fire = false;
		ItemStack tool = player.getHeldItemMainhand();
		if (!isUpgrade(tool, Upgrade.FIRE))
			return false;
		List<ItemStack> copy = Lists.newArrayList(stacks);
		stacks.clear();
		while (!copy.isEmpty()) {
			ItemStack s = copy.remove(0);
			ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
			if (!burned.isEmpty() && !tool.isEmpty()) {
				fire = true;
				stacks.addAll(StackWrapper.toStackList(new StackWrapper(burned, burned.getCount() * s.getCount())));
				if (player.world.rand.nextBoolean())
					damageItem(1, player, tool);
				continue;
			} else
				stacks.add(s);

		}
		return fire;
	}

	private static void handleItemsDefault(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks, Iterable<BlockPos> posses) {
		Vec3d block = new Vec3d(orig.getX() + .5, orig.getY() + .3, orig.getZ() + .5);
		ItemStack tool = player.getHeldItemMainhand();
		if (!player.world.isAirBlock(orig)) {
			for (EnumFacing face : EnumFacing.VALUES) {
				if (face.getAxis().isVertical())
					face = face.getOpposite();
				if (player.world.isAirBlock(orig.offset(face))) {
					block = new Vec3d(orig.offset(face).getX() + .5, orig.offset(face).getY() + .3, orig.offset(face).getZ() + .5);
					break;
				}
			}
		}
		for (ItemStack s : stacks) {
			if (s.isEmpty())
				continue;
			EntityItem ei = new EntityItem(player.world, block.xCoord, block.yCoord, block.zCoord, s.copy());
			player.world.spawnEntity(ei);
			Vec3d vec = new Vec3d(player.posX - ei.posX, player.posY + .5 - ei.posY, player.posZ - ei.posZ).normalize().scale(0.9);
			if (isUpgrade(tool, Upgrade.MAGNET) || isUpgrade(tool, Upgrade.AUTOMINE)) {
				ei.motionX = vec.xCoord;
				ei.motionY = vec.yCoord;
				ei.motionZ = vec.zCoord;
			} else
				ei.motionY = .2;
		}
	}

	public static void damageEntity(ItemStack tool, EntityPlayer player, EntityLivingBase victim) {
		double rad = ToolHelper.isUpgrade(tool, Upgrade.ExE) ? 1.5D : ToolHelper.isUpgrade(tool, Upgrade.SxS) ? 3.0 : 0;
		List<EntityLivingBase> around = victim.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(victim.getPositionVector().addVector(-rad, -rad, -rad), victim.getPositionVector().addVector(rad, rad, rad)));
		double damage = tool.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, tool).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next().getAmount();
		long damageModis = ToolHelper.getUpgradeCount(tool, Upgrade.DAMAGE);
		if (!around.contains(victim))
			around.add(victim);
		for (EntityLivingBase elb : around) {
			if (elb instanceof EntityPlayer)
				continue;
			if (elb != victim) {
				elb.attackEntityFrom(DamageSource.causePlayerDamage(player), (float) (((damage * victim.world.rand.nextDouble()) / 4d) * damageModis));
				if (victim.world.rand.nextBoolean())
					ToolHelper.damageItem(1, player, tool);
			}
			System.out.println(ToolHelper.getUpgrades(tool));
			if (ToolHelper.isUpgrade(tool, Upgrade.POISON)) {
				if (victim.world.rand.nextDouble() < .7)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.FIRE)) {
				if (victim.world.rand.nextDouble() < .8)
					victim.setFire(7);
			} else if (ToolHelper.isUpgrade(tool, Upgrade.SLOW)) {
				if (victim.world.rand.nextDouble() < .6)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(2), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.WITHER)) {
				if (victim.world.rand.nextDouble() < .25)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(20), 140, 2));
			} else if (ToolHelper.isUpgrade(tool, Upgrade.HEAL)) {
				player.heal(victim.world.rand.nextFloat() * 1.2F);
			}
		}
	}

	public static boolean performSkill(ItemStack tool, EntityPlayer player, EnumHand hand, boolean shift) {
		List<ItemStack> lis = NBTStackHelper.getItemStackList(tool, "items");
		if (lis.size() < 9)
			return false;
		ItemStack s = lis.get(!shift ? 7 : 8);
		if (s.isEmpty())
			return false;
		switch (Upgrade.getListForCategory("skill").get(s.getItemDamage())) {
		case GUI:
			if (!player.world.isRemote)
				player.openGui(Tools.instance, ID.TOOL.ordinal(), player.world, 0, 0, 0);
			return true;
		case TORCH:
			final ItemStack TORCH = new ItemStack(Blocks.TORCH);
			RayTraceResult ray = ForgeHooks.rayTraceEyes(player, 30);
			if (ray != null && ray.typeOfHit == Type.BLOCK && ray.sideHit != EnumFacing.DOWN) {
				BlockPos pos = ray.getBlockPos();
				IBlockState iblockstate = player.world.getBlockState(pos);
				EnumFacing facing = ray.sideHit;
				Block block = iblockstate.getBlock();
				if (!block.isReplaceable(player.world, pos)) {
					pos = pos.offset(facing);
				}
				if (player.canPlayerEdit(pos, facing, TORCH) && player.world.mayPlace(Blocks.TORCH, pos, false, facing, (Entity) null) && player.world.isAirBlock(pos)) {
					if (!player.world.isRemote) {
						IBlockState iblockstate1 = Blocks.TORCH.getStateForPlacement(player.world, pos, facing, 0, 0, 0, TORCH.getItemDamage(), player, hand);
						player.world.setBlockState(pos, iblockstate1);
						damageItem(1, player, tool);
					} else {
						Vec3d eye = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
						Vec3d to = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
						Vec3d dir = new Vec3d(to.xCoord - eye.xCoord, to.yCoord - eye.yCoord, to.zCoord - eye.zCoord).scale(.2);
						eye = eye.add(dir.scale(-1).normalize());
						for (int ii = 0; ii < 5; ii++)
							player.world.spawnParticle(EnumParticleTypes.FLAME, eye.xCoord + Utils.getRandomNumber(-.25, .25), eye.yCoord + Utils.getRandomNumber(-.25, .25), eye.zCoord + Utils.getRandomNumber(-.25, .25), dir.xCoord, dir.yCoord, dir.zCoord);
					}
					ItemStack torch = player.isCreative() ? new ItemStack(Blocks.TORCH) : InvHelper.extractItem(new PlayerMainInvWrapper(player.inventory), (Predicate<ItemStack>) (ItemStack st) -> (st.getItem() == Item.getItemFromBlock(Blocks.TORCH)), 1, false);
					if (torch.isEmpty()) {
						TorchPart part = new TorchPart();
						DataPartRegistry reg = DataPartRegistry.get(player.world);
						BlockPos p = reg.nextPos(pos);
						if (!player.world.isRemote)
							if (p == null)
								player.world.setBlockToAir(pos);
							else {
								part.torch = pos;
								reg.addDataPart(p, part, false);
							}
					}
					player.getCooldownTracker().setCooldown(tool.getItem(), 20);
					return true;
				}
			}
			break;
		case BAG:
			if (!player.world.isRemote)
				player.openGui(Tools.instance, ID.BAG.ordinal(), player.world, hand.ordinal(), player.isSneaking() ? 1 : 0, 0);
			return true;
		case PORT:
			RayTraceResult ray1 = ForgeHooks.rayTraceEyes(player, 30);
			if (ray1 != null && ray1.typeOfHit == Type.BLOCK) {
				BlockPos p = null;
				if (ray1.sideHit == EnumFacing.UP) {
					if (player.getPositionVector().distanceTo(new Vec3d(ray1.getBlockPos())) < 3d) {
						for (int i = 0; i < 3; i++) {
							if (player.world.isAirBlock(ray1.getBlockPos().down(i + 1)) && player.world.isAirBlock(ray1.getBlockPos().down(i + 2))) {
								p = ray1.getBlockPos().down(i + 2);
								break;
							}
						}
					}
					if (p == null && player.world.isAirBlock(ray1.getBlockPos().up(2)) && player.world.isAirBlock(ray1.getBlockPos().up(3)))
						p = ray1.getBlockPos().up();
				} else {
					for (int i = 0; i < 3; i++) {
						if (player.world.isAirBlock(ray1.getBlockPos().up(i + 1)) && player.world.isAirBlock(ray1.getBlockPos().up(i + 2))) {
							p = ray1.getBlockPos().up(i + 1);
							break;
						}
					}
				}
				if (p != null) {
					player.fallDistance = 0f;
					if (!player.world.isRemote)
						PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
					player.setPositionAndUpdate(p.getX() + .5, p.getY() + .01, p.getZ() + .5);
					if (!player.world.isRemote)
						PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
					damageItem(2, player, tool);
					player.getCooldownTracker().setCooldown(tool.getItem(), 30);
					return true;
				}
			} else if (ray1 == null || ray1.typeOfHit == Type.MISS) {
				player.fallDistance = 0f;
				Vec3d port = player.getPositionVector().add(player.getLookVec().scale(30D));
				if (!player.world.isRemote)
					PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
				player.setPositionAndUpdate(port.xCoord, port.yCoord, port.zCoord);
				if (!player.world.isRemote)
					PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
				player.motionY = 1D;
				damageItem(2, player, tool);
				player.getCooldownTracker().setCooldown(tool.getItem(), 30);
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
}

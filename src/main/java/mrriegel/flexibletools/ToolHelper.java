package mrriegel.flexibletools;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import mrriegel.flexibletools.handler.GuiHandler.ID;
import mrriegel.flexibletools.item.ItemToolUpgrade;
import mrriegel.flexibletools.item.ItemToolUpgrade.QuarryPart;
import mrriegel.flexibletools.item.ItemToolUpgrade.TorchPart;
import mrriegel.flexibletools.item.ItemToolUpgrade.Upgrade;
import mrriegel.flexibletools.network.MessageParticle;
import mrriegel.limelib.datapart.DataPartRegistry;
import mrriegel.limelib.helper.BlockHelper;
import mrriegel.limelib.helper.EnergyHelper;
import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.StackWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ToolHelper {

	public static final ToolMaterial newMat = EnumHelper.addToolMaterial("new_material", 4, 2048, 7.5f, 3.5f, 0);
	public static Map<Item, Integer> repairMap = Maps.newHashMap();
	static {
		repairMap.put(Items.DIAMOND, 500);
		repairMap.put(Items.GOLD_INGOT, 60);
		repairMap.put(Item.getItemFromBlock(Blocks.OBSIDIAN), 70);
		repairMap.put(Item.getItemFromBlock(Blocks.GOLD_BLOCK), repairMap.get(Items.GOLD_INGOT) * 10);
		repairMap.put(Item.getItemFromBlock(Blocks.DIAMOND_BLOCK), repairMap.get(Items.DIAMOND) * 10);
	}

	public static List<Upgrade> getUpgrades(ItemStack stack) {
		List<Upgrade> lis = NBTStackHelper.getItemStackList(stack, "items").stream().map(s -> s.isEmpty() ? null : ItemToolUpgrade.getUpgrade(s)).filter(u -> u != null).collect(Collectors.toList());
		return lis;
	}

	public static boolean isUpgrade(ItemStack stack, Upgrade upgrade) {
		return getUpgrades(stack).contains(upgrade);
	}

	public static int getUpgradeCount(ItemStack stack, Upgrade upgrade) {
		return Math.min(upgrade.max, (int) getUpgrades(stack).stream().filter(u -> u == upgrade).count());
	}

	public static void breakBlocks(ItemStack tool, EntityPlayer player, BlockPos orig, List<BlockPos> posses) {
		if (player.world.isRemote)
			return;
		int blockBreaked = 0;
		NonNullList<ItemStack> drops = NonNullList.create();
		float origHard = player.world.getBlockState(orig).getBlockHardness(player.world, orig);
		boolean radius = isUpgrade(tool, Upgrade.ExE) || isUpgrade(tool, Upgrade.SxS);
		for (BlockPos pos : posses) {
			if (radius && origHard + 25F < player.world.getBlockState(pos).getBlockHardness(player.world, pos))
				continue;
			if (!tool.isEmpty() && !player.world.isAirBlock(pos) && (pos.equals(orig) || (BlockHelper.isToolEffective(tool, player.world, pos, false) && BlockHelper.canToolHarvestBlock(player.world, pos, tool)))) {
				NonNullList<ItemStack> tmp = BlockHelper.breakBlock(player.world, pos, player.world.getBlockState(pos), player, isUpgrade(tool, Upgrade.SILK), getUpgradeCount(tool, Upgrade.LUCK), true, true);
				for (ItemStack s : tmp)
					StackHelper.addStack(drops, s);
				if (!tmp.isEmpty() || player.world.isAirBlock(pos)) {
					blockBreaked++;
					if (!damageItem(1, player, tool, false))
						break;
				}
			}
		}
		player.addExhaustion(blockBreaked / 13F);
		if (!drops.isEmpty()) {
			if (smeltItems(player, tool, drops))
				for (BlockPos p : posses)
					PacketHandler.sendTo(new MessageParticle(p, MessageParticle.SMELT), (EntityPlayerMP) player);
			handleItems(player, orig, drops);
		}
	}

	public static void breakBlock(ItemStack tool, EntityPlayer player, BlockPos orig, BlockPos pos) {
		breakBlocks(tool, player, orig, Collections.singletonList(pos));
	}

	public static int getDurability(ItemStack tool) {
		if (isUpgrade(tool, Upgrade.ENERGY))
			return (int) (EnergyHelper.getEnergy(tool, null)) / 100;
		else
			return tool.getMaxDamage() - tool.getItemDamage();
	}

	public static boolean damageItem(int damage, @Nullable EntityPlayer player, ItemStack tool, Boolean damageAttack) {
		if (damageAttack == null || damage <= 0) {
		} else if (damageAttack) {
			for (Upgrade u : getUpgrades(tool))
				damage += u.additionalDamageAttack;
		} else {
			for (Upgrade u : getUpgrades(tool))
				damage += u.additionalDamageBreak;
		}
		if (isUpgrade(tool, Upgrade.ENERGY) && EnergyHelper.getEnergy(tool, null) >= damage * 100) {
			if (damage >= 0)
				EnergyHelper.extractEnergy(tool, null, 100 * damage, false);
			else
				EnergyHelper.receiveEnergy(tool, null, -100 * damage, false);
		} else {
			if (player != null && !player.isCreative())
				tool.damageItem(damage, player);
			else
				tool.attemptDamageItem(damage, new Random());
		}
		if (tool.isEmpty() && player != null) {
			for (ItemStack s : NBTStackHelper.getItemStackList(tool, "items")) {
				if (!s.isEmpty())
					player.world.spawnEntity(new EntityItem(player.world, player.posX, player.posY + .3, player.posZ, s));
			}
		}
		return !tool.isEmpty();
	}

	public static void handleItems(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks) {
		ItemStack tool = player.getHeldItemMainhand();
		if (isUpgrade(tool, Upgrade.TELE) && NBTStackHelper.hasTag(tool, "gpos")) {
			GlobalBlockPos gpos = GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos"));
			IItemHandler inv = InvHelper.getItemHandler(gpos.getWorld(), gpos.getPos(), null);
			if (inv == null) {
				handleItemsDefault(player, orig, stacks);
				player.sendMessage(new TextComponentString("Inventory was removed"));
				return;
			}
			NonNullList<ItemStack> set = NonNullList.create();
			for (ItemStack s : stacks)
				StackHelper.addStack(set, ItemHandlerHelper.insertItemStacked(inv, s.copy(), false));
			PacketHandler.sendTo(new MessageParticle(orig, MessageParticle.TELE), (EntityPlayerMP) player);
			handleItemsDefault(player, orig, set);
		} else
			handleItemsDefault(player, orig, stacks);
	}

	private static boolean smeltItems(EntityPlayer player, ItemStack tool, NonNullList<ItemStack> stacks) {
		boolean fire = false;
		if (!isUpgrade(tool, Upgrade.FIRE))
			return false;
		List<ItemStack> copy = Lists.newArrayList(stacks);
		stacks.clear();
		while (!copy.isEmpty()) {
			ItemStack s = copy.remove(0);
			ItemStack burned = FurnaceRecipes.instance().getSmeltingResult(s).copy();
			if (!burned.isEmpty() && !tool.isEmpty()) {
				fire = true;
				for (ItemStack nn : StackWrapper.toStackList(new StackWrapper(burned, burned.getCount() * s.getCount())))
					StackHelper.addStack(stacks, nn);
				if (player.world.rand.nextBoolean())
					damageItem(1, player, tool, null);
				continue;
			} else
				stacks.add(s);

		}
		return fire;
	}

	private static void handleItemsDefault(EntityPlayer player, BlockPos orig, NonNullList<ItemStack> stacks) {
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
			if (isUpgrade(tool, Upgrade.MAGNET) || isUpgrade(tool, Upgrade.AUTOMINE)) {
				ei.setPositionAndUpdate(player.posX, player.posY + .3, player.posZ);
			} else
				ei.motionY = .2;
		}
	}

	public static void damageEntity(ItemStack tool, EntityPlayer player, EntityLivingBase victim) {
		boolean toolBroken = !damageItem(2, player, tool, true);
		double rad = isUpgrade(tool, Upgrade.ExE) ? 1.5D : isUpgrade(tool, Upgrade.SxS) ? 3.0 : 0;
		World world = player.world;
		List<EntityLivingBase> around = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(victim.getPositionVector().addVector(-rad, -rad, -rad), victim.getPositionVector().addVector(rad, rad, rad)));
		float damage = (float) tool.getItem().getAttributeModifiers(EntityEquipmentSlot.MAINHAND, tool).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).iterator().next().getAmount();
		if (!around.contains(victim))
			around.add(victim);
		for (EntityLivingBase elb : around) {
			if (elb instanceof EntityPlayer && elb != victim)
				continue;
			if (toolBroken && elb != victim)
				continue;
			if (isUpgrade(tool, Upgrade.POISON)) {
				if (world.rand.nextDouble() < .7)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 140, 2));
			} else if (isUpgrade(tool, Upgrade.FIRE)) {
				if (world.rand.nextDouble() < .8)
					victim.setFire(7);
			} else if (isUpgrade(tool, Upgrade.SLOW)) {
				if (world.rand.nextDouble() < .6)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(2), 140, 2));
			} else if (isUpgrade(tool, Upgrade.WITHER)) {
				if (world.rand.nextDouble() < .2)
					victim.addPotionEffect(new PotionEffect(Potion.getPotionById(20), 140, 2));
			} else if (isUpgrade(tool, Upgrade.HEAL)) {
				player.heal(world.rand.nextFloat() * .9F);
			}
			if (elb != victim) {
				if (world.rand.nextBoolean()) {
					elb.attackEntityFrom(DamageSource.causePlayerDamage(player), (damage * (float) MathHelper.nextDouble(random, .5, 1.)) / 3f);
					if (world.rand.nextDouble() < .3)
						damageItem(1, player, tool, true);
				}
			}
			if (tool.isEmpty())
				toolBroken = true;
		}
	}

	static Random random = new Random();

	public static boolean performSkill(ItemStack tool, EntityPlayer player, EnumHand hand) {
		List<ItemStack> lis = NBTStackHelper.getItemStackList(tool, "items");
		if (lis.size() < 9)
			return false;
		ItemStack s = lis.get(!player.isSneaking() ? 7 : 8);
		if (s.isEmpty())
			return false;
		switch (Upgrade.getListForCategory("skill").get(s.getItemDamage())) {
		case GUI:
			if (!player.world.isRemote)
				player.openGui(FlexibleTools.instance, ID.TOOL.ordinal(), player.world, hand.ordinal(), 0, 0);
			return true;
		case BAG:
			if (!player.world.isRemote)
				player.openGui(FlexibleTools.instance, ID.BAG.ordinal(), player.world, hand.ordinal(), player.isSneaking() ? 1 : 0, 0);
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
						damageItem(1, player, tool, null);
					} else {
						Vec3d eye = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
						Vec3d to = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
						Vec3d dir = new Vec3d(to.xCoord - eye.xCoord, to.yCoord - eye.yCoord, to.zCoord - eye.zCoord).scale(.2);
						eye = eye.add(dir.scale(-1).normalize());
						for (int ii = 0; ii < 5; ii++)
							player.world.spawnParticle(EnumParticleTypes.FLAME, eye.xCoord + MathHelper.nextDouble(random, -.25, .25), eye.yCoord + MathHelper.nextDouble(random, -.25, .25), eye.zCoord + MathHelper.nextDouble(random, -.25, .25), dir.xCoord, dir.yCoord, dir.zCoord);
					}
					ItemStack torch = player.isCreative() ? new ItemStack(Blocks.TORCH) : InvHelper.extractItem(new PlayerMainInvWrapper(player.inventory), (ItemStack st) -> (st.getItem() == Item.getItemFromBlock(Blocks.TORCH)), 1, false);
					if (torch.isEmpty()) {
						TorchPart part = new TorchPart();
						DataPartRegistry reg = DataPartRegistry.get(player.world);
						BlockPos p = reg.nextPos(pos);
						if (!player.world.isRemote)
							if (p == null) {
								player.world.setBlockToAir(pos);
								damageItem(-1, player, tool, null);
							} else {
								part.torch = pos;
								reg.addDataPart(p, part, false);
							}
					}
					player.getCooldownTracker().setCooldown(tool.getItem(), 20);
					return true;
				}
			}
			break;
		case PORT:
			RayTraceResult ray1 = ForgeHooks.rayTraceEyes(player, 30);
			if (ray1 != null && ray1.typeOfHit == Type.BLOCK) {
				BlockPos p = null;
				if (ray1.sideHit == EnumFacing.UP) {
					if (player.getPositionVector().distanceTo(new Vec3d(ray1.getBlockPos())) < 3d) {
						for (int i = 0; i < 3; i++) {
							if (player.world.isAirBlock(ray1.getBlockPos().down(i + 1)) && player.world.isAirBlock(ray1.getBlockPos().down(i + 2)) && ray1.getBlockPos().down(i + 2).getY() >= 2) {
								p = ray1.getBlockPos().down(i + 2);
								break;
							}
						}
					}
					if (p == null && player.world.isAirBlock(ray1.getBlockPos().up(2)) && player.world.isAirBlock(ray1.getBlockPos().up(3)))
						p = ray1.getBlockPos().up();
				} else {
					if (p == null)
						for (int i = 0; i < 3; i++) {
							if (player.world.isAirBlock(ray1.getBlockPos().up(i + 1)) && player.world.isAirBlock(ray1.getBlockPos().up(i + 2))) {
								p = ray1.getBlockPos().up(i + 1);
								break;
							}
						}
					if (p == null)
						p = ray1.getBlockPos().offset(ray1.sideHit).down();
				}
				if (p != null) {
					Vec3d port = new Vec3d(p.getX() + .5, p.getY() + .01, p.getZ() + .5);
					teleport(player, port, tool);
					return true;
				}
			} else if (ray1 == null || ray1.typeOfHit == Type.MISS) {
				Vec3d port = player.getPositionVector().add(player.getLookVec().scale(30D));
				while (!player.world.isAirBlock(new BlockPos(port))) {
					port = port.addVector(0, 1, 0);
				}
				teleport(player, port, tool);
				player.motionY = 1D;
				return true;
			}
			break;
		case CHUNKMINER:
			RayTraceResult ray2 = ForgeHooks.rayTraceEyes(player, 5);
			if (ray2 != null && ray2.typeOfHit == Type.BLOCK) {
				BlockPos pos = ray2.getBlockPos();
				EnumFacing facing = ray2.sideHit;
				pos = pos.offset(facing);
				if (!NBTStackHelper.hasTag(tool, "gpos") || !isUpgrade(tool, Upgrade.TELE) || !InvHelper.hasItemHandler(GlobalBlockPos.loadGlobalPosFromNBT(NBTStackHelper.getTag(tool, "gpos")).getTile(), null)) {
					if (!player.world.isRemote)
						player.sendMessage(new TextComponentString("No inventory bound OR inventory removed OR receiver upgrade missing."));
					return false;
				}
				if (!player.world.isAirBlock(pos))
					return false;
				QuarryPart part = new QuarryPart();
				DataPartRegistry reg = DataPartRegistry.get(player.world);
				BlockPos p = reg.nextPos(pos);
				part.setTool(tool);
				part.setFuel(NBTStackHelper.getInt(tool, "fuel"));
				player.setHeldItem(hand, ItemStack.EMPTY);
				reg.addDataPart(p, part, false);
				return true;
			} else
				player.sendMessage(new TextComponentString("Aim at a block."));
			break;
		default:
			break;
		}
		return false;
	}

	private static void teleport(EntityPlayer player, Vec3d port, ItemStack tool) {
		player.fallDistance = 0f;
		if (!player.world.isRemote)
			PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
		player.setPositionAndUpdate(port.xCoord, port.yCoord, port.zCoord);
		if (!player.world.isRemote)
			PacketHandler.sendTo(new MessageParticle(new BlockPos(player), MessageParticle.TELE), (EntityPlayerMP) player);
		player.motionY = 1D;
		damageItem(5, player, tool, null);
		player.getCooldownTracker().setCooldown(tool.getItem(), 30);
	}
}

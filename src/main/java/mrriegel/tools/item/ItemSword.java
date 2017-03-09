package mrriegel.tools.item;

import java.util.Set;

import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.tools.ToolHelper;
import mrriegel.tools.handler.CTab;
import mrriegel.tools.item.ItemToolUpgrade.Upgrade;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ItemSword extends net.minecraft.item.ItemSword implements ITool {

	private final ToolMaterial material;

	public ItemSword() {
		super(ToolHelper.newMat);
		material = ToolHelper.newMat;
		setRegistryName("swordi");
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CTab.TAB);
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		if (!worldIn.isRemote && worldIn.rand.nextInt(140) == 0 && ToolHelper.isUpgrade(stack, Upgrade.REPAIR) && entityIn instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entityIn;
			if (stack.getItemDamage() > 0) {
				stack.damageItem(-1, player);
			}
		}
	}

	protected double getAttackDamage(ItemStack stack) {
		double d = 3d + material.getDamageVsEntity();
		for (Upgrade u : ToolHelper.getUpgrades(stack))
			if (u == Upgrade.DAMAGE)
				d += 2.5;
		return d;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (ToolHelper.performSkill(playerIn.getHeldItem(handIn), playerIn, handIn, playerIn.isSneaking())) {
			return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (InvHelper.hasItemHandler(worldIn, pos, null)) {
			NBTTagCompound nbt = new NBTTagCompound();
			new GlobalBlockPos(pos, worldIn).writeToNBT(nbt);
			NBTStackHelper.setTag(player.getHeldItem(hand), "gpos", nbt);
			if (!worldIn.isRemote)
				player.sendMessage(new TextComponentString("Bound to " + worldIn.getBlockState(pos).getBlock().getLocalizedName()));
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (attacker instanceof EntityPlayer) {
			ToolHelper.damageEntity(stack, (EntityPlayer) attacker, target);
		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) getAttackDamage(stack), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
		}
		return multimap;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack) {
		Set<String> set = Sets.newHashSet(super.getToolClasses(stack));
		set.add("sword");
		return set;
	}

}

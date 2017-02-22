package mrriegel.tools.item;

import java.util.Collections;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.common.util.EnumHelper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public abstract class GenericItemTool extends ItemTool {

	public static final ToolMaterial fin = EnumHelper.addToolMaterial("dorphy", 4, 2222, 7.5f, 2.5f, 20);

	private Set<String> classes;

	protected GenericItemTool(ToolMaterial materialIn, String... classes) {
		super(materialIn, Collections.EMPTY_SET);
		if (classes != null)
			this.classes = Sets.newHashSet(classes);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, EntityPlayer player, IBlockState blockState) {
		return getToolClasses(stack).contains(toolClass) ? toolMaterial.getHarvestLevel() : super.getHarvestLevel(stack, toolClass, player, blockState);
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack) {
		return classes != null ? classes : Collections.EMPTY_SET;
	}

	protected final double getBaseDamage(ItemStack stack) {
		if (getToolClasses(stack).contains("shovel"))
			return 1.5f;
		else if (getToolClasses(stack).contains("pickaxe"))
			return 1.0f;
		else if (getToolClasses(stack).contains("axe"))
			return 0.0f;
		return 0f;
	}

	protected final double getBaseSpeed(ItemStack stack) {
		if (getToolClasses(stack).contains("axe"))
			return 0.0f;
		else if (getToolClasses(stack).contains("pickaxe"))
			return -2.8f;
		else if (getToolClasses(stack).contains("shovel"))
			return -3.0f;
		return 0f;
	}

	protected double getAttackDamage(ItemStack stack) {
		return getBaseDamage(stack) + toolMaterial.getDamageVsEntity();
	};

	protected double getAttackSpeed(ItemStack stack) {
		return getBaseSpeed(stack);
	};

	protected float getDigSpeed(ItemStack stack, IBlockState state) {
		return efficiencyOnProperMaterial;
	};

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(stack), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", getAttackSpeed(stack), 0));
		}
		return multimap;
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		for (String type : getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(type, state))
				return getDigSpeed(stack, state);
		}
		return 1.0F;
	}

}

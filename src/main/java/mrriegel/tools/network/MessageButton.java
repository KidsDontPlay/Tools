package mrriegel.tools.network;

import java.util.List;
import java.util.function.Predicate;

import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.NBTStackHelper;
import mrriegel.limelib.network.AbstractMessage;
import mrriegel.tools.gui.ContainerFoodBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class MessageButton extends AbstractMessage<MessageButton> {

	public MessageButton() {
	}

	public MessageButton(int i) {
		NBTHelper.setInt(nbt, "button", i);
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		switch (NBTHelper.getInt(nbt, "button")) {
		case 0:
			if (player.openContainer instanceof ContainerFoodBag) {
				PlayerMainInvWrapper inv = new PlayerMainInvWrapper(player.inventory);
				Predicate<ItemStack> pred = s -> s.getItem() instanceof ItemFood;
				List<ItemStack> baglist = NBTStackHelper.getItemStackList(player.inventory.getCurrentItem(), "items");
				ItemStackHandler bag = new ItemStackHandler(15);
				for (int i = 0; i < baglist.size(); i++)
					bag.setStackInSlot(i, baglist.get(i));
				while (true) {
					ItemStack couldEx = InvHelper.extractItem(inv, pred, 64, true);
					if (couldEx.isEmpty())
						break;
					int canInsert = InvHelper.canInsert(bag, couldEx);
					ItemStack ex = InvHelper.extractItem(inv, pred, canInsert, false);
					ItemHandlerHelper.insertItemStacked(bag, ex, false);
				}
				baglist.clear();
				for (int i = 0; i < bag.getSlots(); i++) {
					baglist.add(bag.getStackInSlot(i));
				}
				NBTStackHelper.setItemStackList(player.inventory.getCurrentItem(), "items", baglist);
				ContainerFoodBag con = (ContainerFoodBag) player.openContainer;
				con.readFromStack();
				con.detectAndSendChanges();
			}
			break;

		default:
			break;
		}

	}
}

package com.natamus.youritemsaresafe.fabric.integration;

import com.natamus.youritemsaresafe.util.Util;
import com.tiviacz.travelersbackpack.attachment.AttachmentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Collects the Traveler's Backpack worn on the player's back on death.
 *
 * The worn backpack lives in TB's own attachment slot — entirely outside the
 * vanilla inventory and equipment slots that YIAS normally inspects. Without
 * this collector the worn backpack bypasses the chest and is processed only by
 * TB's own DeathHandler, which drops it separately.
 *
 * After capturing the stack we call {@code attachment.remove(player)} to clear
 * TB's worn slot. This prevents TB's DeathHandler from also handling the same
 * backpack, which would otherwise cause duplication.
 */
public class TravelersBackpackCollector {

	public static void collect(ServerPlayer player, List<ItemStack> items) {
		if (!AttachmentUtils.isWearingBackpack(player)) {
			return;
		}

		ItemStack backpack = AttachmentUtils.getWearingBackpack(player);
		if (backpack.isEmpty()) {
			return;
		}

		// Clear TB's worn slot first — stops TB's DeathHandler from also seeing it.
		AttachmentUtils.getAttachment(player).ifPresent(attachment -> attachment.remove(player));

		// Save copy BEFORE zeroing original reference.
		ItemStack copy = backpack.copy();
		// Zero the original — if TB already captured a reference, it'll see count=0 and skip the drop.
		backpack.setCount(0);

		if (Util.hasCurseOfVanishing(copy)) {
			// Curse of Vanishing: slot is already cleared; don't add to chest.
			return;
		}

		items.add(copy);
	}
}

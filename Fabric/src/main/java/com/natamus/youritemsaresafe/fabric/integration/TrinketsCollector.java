package com.natamus.youritemsaresafe.fabric.integration;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Collects items from Trinkets slots on player death.
 *
 * Trinkets slots are entirely separate from the vanilla inventory and equipment
 * slots that YIAS inspects. Without this collector, trinket items fall through
 * to Trinkets' own death handling, which drops them on the ground rather than
 * placing them in the chest.
 *
 * We use {@link TrinketAttachment#forEachDroppable} which Trinkets explicitly
 * provides for grave-mod integration. It applies all configured drop rules
 * (per-slot rules, Curse of Vanishing → DESTROY, keepInventory → KEEP) and
 * only iterates items that should be dropped.
 *
 * After collecting each item we clear its slot with
 * {@code slotAccess.set(ItemStack.EMPTY)}. This ensures Trinkets' own death
 * handler finds the slots empty and takes no further action.
 *
 * Items with DESTROY rule (e.g. Curse of Vanishing) are excluded by
 * {@code forEachDroppable} and their slots are handled by a second pass below
 * so Trinkets does not process them either.
 */
public class TrinketsCollector {

	public static void collect(ServerPlayer player, List<ItemStack> items) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(player);

		// Pass 1: collect all items that should drop and clear their slots.
		// keepInventory=false → DEFAULT-rule items get DROP treatment.
		attachment.forEachDroppable((slotAccess, stack) -> {
			if (!stack.isEmpty()) {
				items.add(stack.copy());
				slotAccess.set(ItemStack.EMPTY);
			}
		}, false);

		// Pass 2: clear any remaining non-empty slots (DESTROY-rule items such as
		// Curse of Vanishing). forEachDroppable skips them, but we still need to
		// clear them so Trinkets' own handler does not process them a second time.
		attachment.forEach((slotAccess, stack) -> {
			if (!stack.isEmpty()) {
				TrinketDropRule rule = TrinketsApi.getDropRule(stack, slotAccess, player, false);
				if (rule == TrinketDropRule.DESTROY) {
					slotAccess.set(ItemStack.EMPTY);
				}
			}
		});
	}
}

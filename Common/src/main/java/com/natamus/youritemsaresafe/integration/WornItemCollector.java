package com.natamus.youritemsaresafe.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Callback interface for collecting items from mod-specific worn/equipped slots
 * on player death (e.g. Traveler's Backpack worn slot, Trinkets slots).
 * Implementations are responsible for both adding items to the provided list
 * AND clearing the source slot so the owning mod's death handler does not also
 * drop the same items.
 */
@FunctionalInterface
public interface WornItemCollector {
	void collect(ServerPlayer player, List<ItemStack> items);
}

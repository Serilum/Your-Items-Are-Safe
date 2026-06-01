package com.natamus.youritemsaresafe.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for {@link WornItemCollector} implementations. Loader-specific
 * modules (Fabric, Forge, NeoForge) register collectors here during init for
 * any optional mod integrations they support.
 */
public class Integrations {
	private static final List<WornItemCollector> collectors = new ArrayList<>();

	public static void register(WornItemCollector collector) {
		collectors.add(collector);
	}

	/**
	 * Calls every registered collector, appending their items to {@code items}.
	 * Each collector is also responsible for clearing its source slot.
	 */
	public static void collectAll(ServerPlayer player, List<ItemStack> items) {
		for (WornItemCollector collector : collectors) {
			collector.collect(player, items);
		}
	}
}

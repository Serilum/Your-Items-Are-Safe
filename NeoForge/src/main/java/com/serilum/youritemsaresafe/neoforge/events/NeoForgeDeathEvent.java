package com.serilum.youritemsaresafe.neoforge.events;

import com.serilum.youritemsaresafe.events.DeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class NeoForgeDeathEvent {
	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent e) {
		Entity entity = e.getEntity();
		if (entity.level().isClientSide) {
			return;
		}

		if (!(entity instanceof Player)) {
			return;
		}

		DeathEvent.onPlayerDeath((ServerPlayer)entity, e.getSource(), 0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerDrops(LivingDropsEvent e) {
		Entity entity = e.getEntity();
		if (entity.level().isClientSide) {
			return;
		}

		if (!(entity instanceof ServerPlayer)) {
			return;
		}

		DeathEvent.onPlayerDrops((ServerPlayer)entity, e.getDrops());
	}
}

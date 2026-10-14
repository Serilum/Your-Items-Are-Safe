package com.serilum.youritemsaresafe.forge.events;

import com.serilum.youritemsaresafe.events.DeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class ForgeDeathEvent {
	public static void registerEventsInBus() {
		BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeDeathEvent.class);
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent e) {
		Entity entity = e.getEntity();
		if (entity.level().isClientSide()) {
			return;
		}

		if (!(entity instanceof Player)) {
			return;
		}

		DeathEvent.onPlayerDeath((ServerPlayer)entity, e.getSource(), 0);
	}

	@SubscribeEvent(priority = Priority.LOWEST)
	public static void onPlayerDrops(LivingDropsEvent e) {
		Entity entity = e.getEntity();
		if (entity.level().isClientSide()) {
			return;
		}

		if (!(entity instanceof ServerPlayer)) {
			return;
		}

		DeathEvent.onPlayerDrops((ServerPlayer)entity, e.getDrops());
	}
}

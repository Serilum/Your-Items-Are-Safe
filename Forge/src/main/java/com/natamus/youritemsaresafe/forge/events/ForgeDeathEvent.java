package com.natamus.youritemsaresafe.forge.events;

import com.natamus.youritemsaresafe.events.DeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class ForgeDeathEvent {
	public static void registerEventsInBus() {
		// BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeDeathEvent.class);

		LivingDeathEvent.BUS.addListener(ForgeDeathEvent::onPlayerDeath);
	}

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
}

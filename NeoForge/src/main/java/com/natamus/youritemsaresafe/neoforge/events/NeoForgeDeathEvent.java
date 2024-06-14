package com.natamus.youritemsaresafe.neoforge.events;

import com.natamus.youritemsaresafe.events.DeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
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
}

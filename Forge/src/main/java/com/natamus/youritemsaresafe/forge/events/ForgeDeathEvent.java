package com.natamus.youritemsaresafe.forge.events;

import com.natamus.youritemsaresafe.events.DeathEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ForgeDeathEvent {
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent e) {
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

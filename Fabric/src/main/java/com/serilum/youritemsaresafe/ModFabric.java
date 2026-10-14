package com.serilum.youritemsaresafe;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.check.ShouldLoadCheck;
import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import com.serilum.youritemsaresafe.events.DeathEvent;
import com.serilum.youritemsaresafe.util.Reference;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class ModFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadEvents() {
		CollectivePlayerEvents.PLAYER_DEATH.register((ServerLevel serverLevel, ServerPlayer serverPlayer) -> {
			DeathEvent.onPlayerDeath(serverPlayer, serverPlayer.getLastDamageSource(), 0);
		});

		CollectiveEntityEvents.ON_LIVING_ENTITY_DROPS.register((Level level, Entity entity, DamageSource damageSource, List<ItemEntity> drops) -> {
			if (entity instanceof ServerPlayer) {
				DeathEvent.onPlayerDrops((ServerPlayer)entity, drops);
			}
		});
	}
}

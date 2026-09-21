package com.mcdiamondfire.modapi.fabric.internal;

import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModEntrypoint implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
		InternalRuntime.initialize();
	}
	
}

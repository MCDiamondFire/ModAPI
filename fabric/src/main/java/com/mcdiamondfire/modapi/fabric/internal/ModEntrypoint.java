package com.mcdiamondfire.modapi.fabric.internal;

import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModEntrypoint implements ClientModInitializer {
	
	public ModEntrypoint() {
	}
	
	@Override
	public void onInitializeClient() {
		InternalRuntime.initialize();
	}
	
}

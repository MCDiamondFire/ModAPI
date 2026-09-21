# DiamondFire ModAPI

This repository contains both the Minecraft-independent ModAPI protocol and its Fabric client mod integration.

> [!WARNING]
> This repository is a work in progress, you cannot use this yet. Packets may be changed, added, and removed at any
time, and this repository may go unmaintained for long periods of time. No promises are made.

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for information about contributing.

## Structure

- `mod-api`: the independent protocol, generated Protocol Buffer messages, and binary directional frames.
- `mod-api-fabric`: the supported Java API for Fabric mods. It exposes typed operations, results, and models.

## Usage

Fabric mods should bundle the integration using Loom jar-in-jar. Players do not
need to install it separately. Fabric Loader deduplicates nested copies by mod ID,
so several mods can bundle it safely; do not relocate it.

```kotlin
dependencies {
	modImplementation("com.mcdiamondfire.modapi:mod-api-fabric:1.0.0")
	include("com.mcdiamondfire.modapi:mod-api-fabric:1.0.0")
}
```

Mods using the API should also declare the integration as a dependency:

```json5
{
  // ...
  "depends": {
    "diamondfire_modapi": ">=1.0.0"
  },
  // ...
}
```

## Example

```java
@Override
public void onInitializeClient() {
	// Listening to events.
	ModAPI.ON_MODE_SWITCH.register(mode -> LOGGER.info("Mode changed to {}", mode));

	// Executing operations.
	ModAPI.CODE.get(blockPos)
			.thenAccept(templateJson ->
                    LOGGER.info("Copied template: {}", templateJson)
            );
}
```

# Bedwars Upgrades Display (Legacy Fabric / Ornithe 1.8.9 + OneConfig V1)

Port from Forge 1.8.9 to Fabric/Ornithe 1.8.9, cross-checked against two real, published OneConfig
mods for this exact platform:
- [saadndm/PolySprint](https://github.com/saadndm/PolySprint/blob/legacy/build.gradle.kts) (build script reference, as originally requested)
- [TomekoYT/HyBedWars](https://github.com/TomekoYT/HyBedWars) — a real Bedwars companion mod on this exact platform, which turned out to diverge from PolySprint in several important ways corrected here

Mappings: [mmv.wagyourtail.xyz](https://mmv.wagyourtail.xyz/?q=%22%22&MCV=%221.8.9%22)

## What "legacy fabric 1.8.9" actually means here

This targets **Ornithe** (not the separate "Legacy Fabric Loader" project) — the Feather/MCP mappings + `ploceus`/`fabric-loom-remap` Gradle plugin ecosystem both reference repos use for 1.8.9. OneConfig **V1** (`org.polyfrost.oneconfig`) is used because it publishes a `1.8.9-ornithe` build, unlike Forge.

## Corrections made after cross-referencing HyBedWars (a closer analog than PolySprint)

An earlier draft of this port was built from PolySprint alone and got several things wrong that only became clear once HyBedWars — an actual Bedwars mod on the identical platform — was checked too:

1. **Mappings**: switched from Feather (`feather-gen2`) to **MCP-stable** (`ploceus.mcpMappings("stable", "1.8.9", "22")`), matching HyBedWars. This also happens to match the mappings the original Forge source was already written against (`IChatComponent.getUnformattedText()` etc.), so almost nothing needed renaming.
2. **Entrypoint**: NOT Fabric's `ClientModInitializer`. Pre-1.14 Ornithe targets use **OSL** (Ornithe Standard Library)'s own `net.ornithemc.osl.entrypoints.api.ModInitializer` with an `init()` method, confirmed directly from HyBedWars's own `HyBedWars.kt`. `fabric.mod.json`'s entrypoint key is `"init"`, not `"client"`, and the build now depends on OSL (`ploceus.dependOsl(oslVersion)`).
3. **Chat listening**: OneConfig's cross-platform `ChatEvent`/`EventManager` was the first approach tried, but HyBedWars doesn't use it at all for this platform — it hooks the raw 1.8.9 packet handler directly via a mixin (`NetHandlerPlayClientMixin`, injecting into `NetHandlerPlayClient.handleChat`). This port now does the same, just simplified since we only ever read the message and never cancel/rewrite it.
4. **HUD rendering — the biggest one**: OneConfig V1's Compose-based `Hud`/`TextHud` (`@Composable fun Content()`) is for modern Fabric targets. On Ornithe/1.8.9 specifically, HyBedWars's own `BedwarsResourceDisplay.kt` shows it extends **`LegacyHud`** instead, with plain immediate-mode rendering (`mc.fontRendererObj.drawString(...)`) — much closer to the original Forge `HUDRenderer`. This port's `BwUpgradesHud` now extends `LegacyHud` the same way. Background/text color customization is still inherited free from the base `Hud` class either way (editable via OneConfig's HUD Design Studio) — `LegacyHud` only changes how content is drawn, not the surrounding options.
5. Because of point 4, **the mod is entirely Java again** — no Kotlin source needed anywhere (the Kotlin Gradle plugin was removed from the build accordingly), even though it still depends on `fabric-language-kotlin` at runtime since OneConfig's own classes are Kotlin.
6. **Version pins bumped** to HyBedWars's more current ones: `oneconfig_version` 1.2.0 (was 1.1.12), `fabric_loader_version` 0.19.5 (was 0.19.3), and **Java 25** (both real repos pin this exact version for this exact 1.8.9-ornithe target specifically, not just as a multi-version fallback).
7. **`fabric.mod.json` depends key**: HyBedWars uses `"oneconfig"`, while PolySprint's own real file used `"oneconfigv1"` — a genuine discrepancy between the two repos I can't resolve without building. This port uses `"oneconfig"` (HyBedWars's, being the more current pin), but if the build or a "missing dependency" error at launch complains about this key, try swapping it for `"oneconfigv1"`.
8. **Missing repositories**: an earlier version of this build dropped `google()` and `maven.cloverclient.com/releases` when I rewrote the repositories block to match HyBedWars's simpler-looking one — turns out `cloverclient.com` is exactly what hosts the huge transitive Compose Multiplatform/androidx dependency tree that OneConfig's `-ornithe` artifact pulls in (even though our own code only uses `LegacyHud`, not Compose directly — the library itself still depends on the Compose stack internally). Restored both to match HyBedWars's real `repositories {}` block exactly.
9. **`Hud.Category` predefined constants aren't accessible from Java**: `Category.INFO` failed first ("private access"), and after switching to `Category.COMBAT` (matching HyBedWars's own `BedwarsResourceDisplay.kt`) that failed the exact same way too. This is a classic Kotlin/Java interop symptom: a companion-object member without `@JvmStatic` in the actually-compiled artifact is only reachable from other Kotlin code via special compiler handling, not as a plain static field the way Java needs. Rather than guess a third name, `BwUpgradesHud` now constructs its own `Category` directly (`new Category("Bedwars Upgrades Display", (byte) 50)`) — `Category`'s own primary constructor is a plain public constructor, not a companion member, so it isn't subject to this issue.
10. **Text color/shadow/background aren't inherited free from the base `Hud` class for `LegacyHud`** — an earlier version of this file assumed they were (via `getTextColor()` etc.), but that's apparently only automatic for the Compose-based `Hud`/`TextHud` path, not the legacy one. `BedwarsResourceDisplay.kt`'s own fields (`txtColor`, `textShadow`, `textShadowColor`, `background`, `backgroundColor`, plus manual `Gui.drawRect`/two-pass `drawString` calls for the shadow effect) show these need to be declared as this HUD's own `@Color`/`@Switch`/`@Slider` option fields and drawn by hand — exactly what `BwUpgradesHud` now does. `showTitles` also moved from `BwUpgradesConfig` onto the HUD itself alongside these, matching HyBedWars's pattern of keeping a HUD's own display options on the HUD class rather than a separate config.
11. **`ClientConnectionEvents.DISCONNECT.register(...)` (OSL's networking event, added for `HypixelPackets`) takes a zero-argument callback**, not the 2-arg `(handler, client)` shape Fabric API's own (different) connection-event class uses — a real build error caught this. HyBedWars's actual Kotlin line (`ClientConnectionEvents.DISCONNECT.register { disableHypixel() }`) never references any implicit parameter, which was the tell.
12. **That 0-arg guess was also wrong** — a second real build error caught it too. Rather than guess a third time, I cloned OrnitheMC/ornithe-standard-libraries directly: `DISCONNECT` is `Event<Consumer<DisconnectContext>>`, i.e. exactly one parameter. Fixed to `register(context -> setInBedwars(false))`. While there, I also cloned HypixelDev/ModAPI and HypixelDev/HypixelData directly to verify the rest of `HypixelPackets.java` (`createHandler`/`subscribeToEventPacket`, `ClientboundLocationPacket.getServerType(): Optional<ServerType>`, `ServerType.getName()`, and that the Bedwars `GameType` constant's name is literally `"Bed Wars"`) against real source rather than leaving them as untested guesses too — all confirmed correct as originally written.

## Still-unverified risk areas

- `LegacyHud.render()`'s exact signature: the copy of OneConfig's source I could read directly (its `minecraft` module) shows `render(mcCtx: GuiGraphicsExtractor)` — a modern-Minecraft-only parameter type that doesn't exist on 1.8.9. HyBedWars's own `//? if ornithe` source branch shows a **no-argument** `render()` for this platform, which is what this port uses — but this means the actual Ornithe-specific binding lives in a different part of OneConfig's build I didn't locate, so I'm trusting HyBedWars's real, compiling code over the source I could see directly. If this is the wrong signature, the compiler error will point exactly at this method in `BwUpgradesHud.java`.
- Everything else (Ploceus/Loom/Bloom plugin versions, OSL version, the plugin repository list) is copied from HyBedWars's real, currently-working files — but I still have no way to actually run this Gradle setup myself, so treat the first build as the real test.

## Bedwars detection via Hypixel's Mod API

Added `HypixelPackets.java`, ported from HyBedWars's own `HypixelPackets.kt`. Hypixel's official Mod API (`org.polyfrost:mod-api-fabric`) sends a real `ClientboundLocationPacket` with the actual server type (e.g. `"Bed Wars"`) whenever it changes — much more reliable than matching chat text. This is used two ways:

- `BwUpgradesHud` now only shows real tracker data while `HypixelPackets.isInBedwars()` is true (still shows placeholder content in the HUD editor regardless).
- Leaving Bedwars (server type changing away from `"Bed Wars"`, or disconnecting) only **hides** the HUD — the tracked upgrades/traps are kept. When Bedwars is entered again, the Hypixel server name from the location packet is compared with the one you left: same server within **Remember Upgrades After Leaving** (default 120s, set 0 to always clear) means you rejoined the same game, so the data is kept; otherwise it's a different game and the data is cleared first.
- The chat-phrase reset in `ChatListener` now only fires on the end-of-game summary (`"1st Killer"`). `"Sending you to"` / `"You are now connected to"` / `"joined the lobby!"` were removed from it because Hypixel prints them on `/rejoin` too, which wiped the data.
- Limitation: upgrades/traps your team bought while you were disconnected can't be seen, since they only arrive as chat messages.
- The tracker maps are now `ConcurrentHashMap`s, since the location packet can arrive on a different thread than the HUD render.

## Requirements

- JDK 25 to build (Ornithe/Fabric mods for old Minecraft versions run on a modern JDK — no separate JDK 8 needed here, unlike Forge).
- Fabric Loader (Ornithe-compatible), OSL, and OneConfig V1 installed in your 1.8.9 instance. `fabric-language-kotlin` is needed too (OneConfig's own classes are Kotlin) even though this mod's own source is plain Java.

## Usage in-game

`/bwupgradesdisplay` opens the settings screen directly. The HUD itself (position, background, text color, scale, etc.) is edited through OneConfig's own HUD Design Studio, same as any other OneConfig HUD.

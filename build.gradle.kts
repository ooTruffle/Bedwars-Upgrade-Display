plugins {
    // Ornithe's own Loom fork + its "Ploceus" plugin — this is what "legacy
    // fabric 1.8.9" actually resolves to for OneConfig-based mods, per
    // github.com/saadndm/PolySprint and github.com/TomekoYT/HyBedWars (both
    // real, published OneConfig mods for 1.8.9), rather than the separately
    // maintained Legacy Fabric Loader project.
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("ploceus") version "1.17-SNAPSHOT"
    id("dev.deftu.gradle.bloom") version "0.2.0"
}

ploceus {
    setIntermediaryGeneration(2)
}

val modId = "bwupgradesdisplay"
val modName = "BedwarsUpgradesDisplay"
val modVersion = "1.0.0"
val mcVersion = "1.8.9"
val loaderVersion = "0.19.5"
val oslVersion = "0.21.0"
val oneconfigVersion = "1.2.0"
// Java version HyBedWars actually pins for this exact 1.8.9-ornithe target
// (checked in its versions/1.8.9-ornithe/gradle.properties) — Ornithe mods
// for old Minecraft versions run fine on a modern JDK, unlike legacy Forge.
val javaVersion = 25

version = "$modVersion+$mcVersion"
group = "com.ootruffle"
base.archivesName.set(modId)

repositories {
    mavenCentral()
    google()

    // This mirrors/hosts the full Compose Multiplatform + androidx dependency
    // tree that OneConfig's "-ornithe" artifact pulls in transitively (even
    // though our own code only uses LegacyHud, not Compose directly — the
    // library itself still depends on it internally). Dropping this line was
    // the cause of the "Could not find pl.tomgirl:lenis" / androidx.* errors.
    maven("https://maven.cloverclient.com/releases")
    maven("https://repo.papermc.io/repository/maven-public/")

    maven("https://maven.fabricmc.net/")
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    // MCP-stable mappings (via ploceus), not Feather — matches HyBedWars,
    // and matches the mappings the original Forge mod's source was already
    // written against (e.g. IChatComponent.getUnformattedText()), so field
    // and method names carry over with effectively no adjustment needed.
    mappings(ploceus.mcpMappings("stable", "1.8.9", "22"))

    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    // OSL (Ornithe Standard Library) provides the ModInitializer entrypoint
    // interface pre-1.14 Ornithe targets use instead of Fabric's own
    // ClientModInitializer.
    ploceus.dependOsl(oslVersion)

    // OneConfig V1: uses the "-ornithe" platform artifact for 1.8.9, since V1
    // has no Forge 1.8.9 build but does support Fabric/Ornithe for 1.8.9.
    modImplementation("org.polyfrost.oneconfig:$mcVersion-ornithe:$oneconfigVersion")
    for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "ui", "utils", "hud")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigVersion")
    }

    // Hypixel's official Mod API — used for reliable "am I in a Bedwars
    // game" detection (a real ClientboundLocationPacket with server type),
    // instead of relying only on chat-string matching. Version/coordinate
    // pinned to what HyBedWars itself uses for this exact platform.
    modImplementation("org.polyfrost:mod-api-fabric:1.0.2+build.2+mc$mcVersion")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.all {
        runDirectory = rootProject.file("run")
    }
    runConfigs.remove(runConfigs["server"])
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

bloom {
    replacement("@MOD_ID@", modId)
    replacement("@MOD_NAME@", modName)
    replacement("@MOD_VERSION@", modVersion)
}

tasks.processResources {
    val props = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "loader_version" to loaderVersion,
        "osl_version" to oslVersion,
        "oneconfig_version" to oneconfigVersion,
        "java_version" to javaVersion.toString()
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "mixins.$modId.json")) {
        expand(props)
    }
}

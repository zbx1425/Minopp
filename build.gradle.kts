plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base") version "0.8.5"

    `maven-publish`
}

val mcVersion = property("mcVersion")!!.toString()
val mcSemverVersion = stonecutter.current.version
val versionWithoutMC = property("modVersion")!!.toString()

val isAlpha = "alpha" in versionWithoutMC
val isBeta = "beta" in versionWithoutMC

// loader stuff
val isFabric = modstitch.isLoom
val isNeoforge = modstitch.isModDevGradleRegular
val isForge = modstitch.isModDevGradleLegacy
val isForgeLike = modstitch.isModDevGradle
val loader = when {
    isFabric -> "fabric"
    isNeoforge -> "neoforge"
    isForge -> "forge"
    else -> error("Unknown loader")
}

modstitch {
    minecraftVersion = mcVersion

    parchment {
        prop("parchment.version") { mappingsVersion = it }
        prop("parchment.minecraft") { minecraftVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "minopp"
        modName = "Minopp"
        modDescription = "Recreation of a classical card game in Minecraft"
        modVersion = "$versionWithoutMC+${stonecutter.current.project}"
        modGroup = "cn.zbx1425"
        modLicense = "MIT"
        modAuthor = "Zbx1425, MalayP"

        prop("meta.mcDep") { replacementProperties.put("mc", it) }
        prop("meta.loaderDep") { replacementProperties.put("loaderVersion", it) }
        prop("deps.fabricApi") { replacementProperties.put("fapi", it) }

        fun <K: Any, V: Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://github.com/zbx1425/minopp/issues")
            put("pack_format", when (property("mcVersion")) {
                "1.20.1" -> "15"
                "1.21.1" -> "48"
                "26.1.2" -> "101.1"
                else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            })
        }
    }

    // Fabric Loom (Fabric)
    loom {
        prop("deps.fabricLoader", required = true) { fabricLoaderVersion = it }

        configureLoom {
            mixin.useLegacyMixinAp = false
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        prop("deps.neoforge") { neoForgeVersion = it }
        prop("deps.forge") { forgeVersion = it }

        configureNeoForge {
            runs {
                register("client") {
                    client()
                    sourceSet = sourceSets.main.get()
                    gameDirectory = layout.projectDirectory.dir("../../run")
                }
                register("server") {
                    server()
                    sourceSet = sourceSets.main.get()
                    gameDirectory = layout.projectDirectory.dir("../../run")
                }
            }

            mods {
                register("main") {
                    sourceSet(sourceSets.main.get())
                }
            }
        }
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true

        configs.register("minopp")

        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        if (isFabric) configs.register("minopp.fabric")
        if (isForgeLike) configs.register("minopp.forgelike")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]
stonecutter {
    constants {
        put("fabric", modstitch.isLoom)
        put("neoforge", modstitch.isModDevGradleRegular)
        put("forge", modstitch.isModDevGradleLegacy)
        put("forgelike", modstitch.isModDevGradle)
    }

    dependencies {
        put("fapi", (findProperty("deps.fabricApi")?.toString() ?: "0.0.0"))
    }

    replacements {
        string {
            direction = eval(current.version, ">=1.21.11")
            replace("ResourceLocation", "Identifier")
        }
        string {
            direction = eval(current.version, ">=1.21.11")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
        }
        string {
            direction = eval(current.version, ">=26.1")
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("renderBackground", "extractBackground")
            replace("guiGraphics.drawString", "guiGraphics.text")
            replace("guiGraphics.drawCenteredString", "guiGraphics.centeredText")
        }
        string {
            direction = eval(current.version, ">=1.21")
            replace("net.minecraftforge", "net.neoforged.neoforge")
            replace("net.minecraftforge.eventbus", "net.neoforged.bus")
            replace("net.minecraftforge.fml", "net.neoforged.fml")
        }
    }
}

// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
dependencies {
    fun Dependency?.jij() = this?.also(::modstitchJiJ)

    prop("deps.mixinExtras") {
        when {
            isFabric -> modstitchImplementation("io.github.llamalad7:mixinextras-fabric:$it").jij()
            isNeoforge -> implementation("io.github.llamalad7:mixinextras-neoforge:$it").jij()
            isForge -> {
                compileOnly("io.github.llamalad7:mixinextras-common:$it")
                implementation("io.github.llamalad7:mixinextras-forge:$it").jij()
            }
            else -> error("Unknown loader")
        }
    }

    fun modDependency(
        id: String,
        artifactGetter: (String) -> String,
        requiredByDependants: Boolean = false,
        supportsRuntime: Boolean = true,
        extra: (Boolean) -> Unit = {}
    ) {
        prop("deps.$id") { modVersion ->
            val noRuntime = prop("deps.$id.noRuntime") { it.toBoolean() } == true
            require(noRuntime || supportsRuntime) { "No runtime is not supported for $id" }

            val configuration = if (requiredByDependants) {
                if (noRuntime) "modstitchModCompileOnlyApi" else "modstitchModApi"
            } else {
                if (noRuntime) "modstitchModCompileOnly" else "modstitchModImplementation"
            }

            configuration(artifactGetter(modVersion))

            extra(!noRuntime)
        }
    }

    if (isFabric) {
        modDependency("fabricApi", { "net.fabricmc.fabric-api:fabric-api:$it" }, requiredByDependants = true)
    }
    if (isNeoforge) {
//        modstitchModRuntimeOnly("thedarkcolour:kotlinforforge-neoforge:${findProperty("deps.kotlinForForge")}")
    }

    // Touhou Little Maid
    if (isNeoforge && mcSemverVersion == "1.21.1") {
        modstitchModImplementation("maven.modrinth:R0bDWFAW:tXG1TkGx")
    }
    if (isForge && mcSemverVersion == "1.20.1") {
        modstitchModImplementation("maven.modrinth:R0bDWFAW:g1SKoGQJ")
    }

    modDependency("yacl", { "dev.isxander:yet-another-config-lib:$it" })
}

fun <T> prop(property: String, required: Boolean = false, ifNull: () -> String? = { null }, block: (String) -> T?): T? {
    return ((System.getenv(property) ?: findProperty(property)?.toString())
        ?.takeUnless { it.isBlank() }
        ?: ifNull())
        .let { if (required && it == null) error("Property $property is required") else it }
        ?.let(block)
}

publishing {
    publications {
        register<MavenPublication>("mod") {
            from(components["java"])

            groupId = "cn.zbx1425"
            artifactId = "minopp"
            version = modstitch.metadata.modVersion.get()

            pom {
                name = modstitch.metadata.modName
                description = modstitch.metadata.modDescription
                url = "https://github.com/zbx1425/minopp"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/zbx1425/Minopp/blob/master/LICENSE.txt"
                    }
                }
                developers {
                    developer {
                        id = "zbx1425"
                        name = "Zbx1425"
                        email = "support@zbx1425.cn"
                    }
                }
                scm {
                    url = "https://github.com/zbx1425/minopp"
                    connection = "scm:git:git//github.com/zbx1425/minopp.git"
                    developerConnection = "scm:git:ssh://git@github.com/zbx1425/minopp.git"
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}


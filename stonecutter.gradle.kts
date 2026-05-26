plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1-fabric"

stonecutter {
    parameters {
        constants {
            fun String.propDefined() = project(node.metadata.project).findProperty(this)?.toString()?.isNotBlank() ?: false

            put("controlify", "deps.controlify".propDefined())
            put("mod-menu", "deps.modMenu".propDefined())
        }
    }
}

version = property("modVersion") as String

tasks.register("clean") {
    group = "build"
    delete(layout.buildDirectory.dir("finalJars"))
}

allprojects {
    repositories {
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")

        maven("https://maven.isxander.dev/releases")
        exclusiveContent {
            forRepository { maven("https://maven.quiltmc.org/repository/release") }
            filter { includeGroupAndSubgroups("org.quiltmc") }
        }
        exclusiveContent {
            forRepository { maven("https://thedarkcolour.github.io/KotlinForForge/") }
            filter { includeGroup("thedarkcolour") }
        }

        exclusiveContent {
            forRepository { maven("https://archive.teacon.cn/maven") }
            filter { includeGroup("org.teacon") }
        }
        flatDir {
            dir(rootDir.resolve("libs"))
        }

        mavenCentral()
    }
}
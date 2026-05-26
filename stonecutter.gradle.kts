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
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
    }
}
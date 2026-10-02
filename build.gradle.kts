@file:Suppress("unused")

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("fabric-loom") version "1.18.1" apply false
    id("com.modrinth.minotaur") version "2.10.0" apply false
    id("maven-publish")
}

repositories {
    mavenCentral()
}

private val changelog = project.file("CHANGELOG.md").readText()
private val readme = project.file("README.md").readText()

private data class McInformation(val base: String, val dependency: String, val fabricModJson: String) {
    companion object {
        fun snapshot(v: String, num: Int): McInformation = McInformation(v, "$v-snapshot-$num", "$v-alpha.$num")
        fun pre(v: String, num: Int): McInformation = McInformation(v, "$v-pre-$num", "$v-pre.$num")
        fun rc(v: String, num: Int): McInformation = McInformation(v, "$v-rc-$num", "$v-rc.$num")
        fun release(v: String): McInformation = McInformation(v, v, v)
    }
}
private data class Deps(val dFabric: String? = null, val dYacl: String? = null, val dModMenu: String? = null) {
    fun fabric(new: String): Deps = Deps(new, dYacl, dModMenu)
    fun yacl(new: String): Deps = Deps(dFabric, new, dModMenu)
    fun modmenu(new: String): Deps = Deps(dFabric, dYacl, new)
}
private fun d() = Deps()

private fun prConfigure(mcInfo: McInformation, maxExclusiveVersion: String, deps: Deps) {
    project(":${mcInfo.base}") {
        extensions.extraProperties.apply {
            set("mc_version", mcInfo.dependency)
            set("min_mc_version", mcInfo.fabricModJson)
            set("max_exc_version", maxExclusiveVersion)
            set("mod_version", "${project.findProperty("general_version")}+${mcInfo.base}")
            set("changelog", changelog)
            set("readme", readme)

            if (deps.dFabric != null) set("fabric_version", deps.dFabric)
            if (deps.dYacl != null) set("yacl_version", deps.dYacl)
            if (deps.dModMenu != null) set("modmenu_version", deps.dModMenu)
        }
    }
}
private fun prConfigure(v: String, maxExv: String, deps: Deps) = prConfigure(McInformation.release(v), maxExv, deps)

private fun String.snapshot(num: Int): McInformation = McInformation.snapshot(this, num)
private fun String.pre(num: Int): McInformation = McInformation.pre(this, num)
private fun String.rc(num: Int): McInformation = McInformation.rc(this, num)

prConfigure("26.3", "26.4", d().fabric("0.161.0+26.3").yacl("3.9.7+26.3-fabric").modmenu("21.0.0"))

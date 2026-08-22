import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("net.fabricmc.fabric-loom")
    id("com.modrinth.minotaur")
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val javaVersion = 25
java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
    withJavadocJar()
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository { maven("https://maven.quiltmc.org/repository/release") }
        filter { includeGroupAndSubgroups("org.quiltmc") }
    }
    mavenCentral()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("mc_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    implementation("maven.modrinth:yacl:${project.property("yacl_version")}")
    implementation("maven.modrinth:modmenu:${project.property("modmenu_version")}")

    implementation("org.quiltmc.parsers:gson:0.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("min_mc_version", project.property("min_mc_version"))
    inputs.property("max_exc_version", project.property("max_exc_version"))
    inputs.property("loader_version", project.property("loader_version"))
    inputs.property("fabric_version", project.property("fabric_version"))
    inputs.property("yacl_version", project.property("yacl_version"))
    inputs.property("modmenu_version", project.property("modmenu_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand("version" to project.version,
            "min_mc_version" to project.property("min_mc_version")!!,
            "max_exc_version" to project.property("max_exc_version")!!,
            "loader_version" to project.property("loader_version")!!,
            "fabric_version" to project.property("fabric_version")!!,
            "yacl_version" to project.property("yacl_version")!!,
            "modmenu_version" to project.property("modmenu_version")!!,
            "kotlin_loader_version" to project.property("kotlin_loader_version")!!)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("modsyncer")
    versionNumber.set(project.version as String)
    versionType.set("beta")
    uploadFile.set(tasks.jar)
    additionalFiles {
        sourcesJar(tasks.kotlinSourcesJar)
        javadocJar(tasks.named("javadocJar"))
    }
    changelog.set(project.property("changelog") as String)
    syncBodyFrom.set(project.property("readme") as String)
    gameVersions.addAll("26.2")
    loaders.add("fabric")
    dependencies {
        required.version("fabric-api", project.property("fabric_version") as String)
        required.version("yacl", project.property("yacl_version") as String)
        required.version("fabric-language-kotlin", project.property("kotlin_loader_version") as String)
        optional.project("modmenu")
    }
}
tasks.named("modrinth") {
    dependsOn("modrinthSyncBody")
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
kotlin {
    jvmToolchain(javaVersion)
}
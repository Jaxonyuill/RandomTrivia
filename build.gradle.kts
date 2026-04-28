plugins {
    kotlin("jvm") version "2.1.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.milkbowl.vault:VaultUnlockedAPI:2.16")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
        jvmArgs("-Xms2G", "-Xmx2G")

        // Automatically accept the Mojang/Paper EULA so the server boots without prompts
        systemProperty("com.mojang.eula.agree", "true")
    }


    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

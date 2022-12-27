plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.11.0"
    id("org.jetbrains.kotlin.jvm") version "1.7.22"
}

group = "ss.tsGen"
version = "1.08"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("java","Kotlin"))
}

tasks {

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

}

dependencies{
//    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
//    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
//    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.7.20")
}
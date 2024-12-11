/*
 * Copyright 2024 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.extensions.stdlib.capitalized
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.notExists

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.downloadTask)
    `maven-publish`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

operator fun DirectoryProperty.div(name: String): Path = get().asFile.toPath() / name

val ensureBuildDirectory: Task = tasks.create("ensureBuildDirectory") {
    val path = layout.buildDirectory.get().asFile.toPath()
    doLast { path.createDirectories() }
    onlyIf { path.notExists() }
}

fun downloadBonxaiBinariesTask(platform: String, arch: String): Download =
    tasks.create<Download>("downloadBonxaiBinaries${platform.capitalized()}${arch.replace("-", "").capitalized()}") {
        group = "bonxaiBinaries"
        dependsOn(ensureBuildDirectory)
        val fileName = "build-$platform-$arch-debug.zip"
        src("https://git.karmakrafts.dev/api/v4/projects/349/packages/generic/build/${libs.versions.bonxai.get()}/$fileName")
        val destPath = layout.buildDirectory / "bonxai" / fileName
        dest(destPath.toFile())
        overwrite(true) // Always overwrite when downloading binaries
        onlyIf { destPath.notExists() }
    }

val downloadBonxaiBinariesWindowsX64: Download = downloadBonxaiBinariesTask("windows", "x64")
val downloadBonxaiBinariesLinuxX64: Download = downloadBonxaiBinariesTask("linux", "x64")
val downloadBonxaiBinariesLinuxArm64: Download = downloadBonxaiBinariesTask("linux", "arm64")
val downloadBonxaiBinariesMacosX64: Download = downloadBonxaiBinariesTask("macos", "x64")
val downloadBonxaiBinariesMacosArm64: Download = downloadBonxaiBinariesTask("macos", "arm64")
val downloadBonxaiBinariesAndroidX86_64: Download = downloadBonxaiBinariesTask("android", "x86_64")
val downloadBonxaiBinariesAndroidArm64V8a: Download = downloadBonxaiBinariesTask("android", "arm64-v8a")
val downloadBonxaiBinariesAndroidArmEabiV7a: Download = downloadBonxaiBinariesTask("android", "armeabi-v7a")
val downloadBonxaiBinariesIosOs64: Download = downloadBonxaiBinariesTask("ios", "os64")
val downloadBonxaiBinariesIosSimulator64: Download = downloadBonxaiBinariesTask("ios", "simulator64")
val downloadBonxaiBinariesIosSimulatorArm64: Download = downloadBonxaiBinariesTask("ios", "simulatorarm64")

fun extractBonxaiBinariesTask(platform: String, arch: String): Copy =
    tasks.create<Copy>("extractBonxaiBinaries${platform.capitalized()}${arch.replace("-", "").capitalized()}") {
        group = "bonxaiBinaries"
        val downloadTaskName = "downloadBonxaiBinaries${platform.capitalized()}${arch.replace("-", "").capitalized()}"
        dependsOn(downloadTaskName)
        val platformPair = "$platform-$arch"
        from(zipTree((layout.buildDirectory / "bonxai" / "build-$platformPair-debug.zip").toFile()))
        val destPath = layout.buildDirectory / "bonxai" / platformPair
        into(destPath.toFile())
        onlyIf { destPath.notExists() }
    }

val extractBonxaiBinariesWindowsX64: Copy = extractBonxaiBinariesTask("windows", "x64")
val extractBonxaiBinariesLinuxX64: Copy = extractBonxaiBinariesTask("linux", "x64")
val extractBonxaiBinariesLinuxArm64: Copy = extractBonxaiBinariesTask("linux", "arm64")
val extractBonxaiBinariesMacosX64: Copy = extractBonxaiBinariesTask("macos", "x64")
val extractBonxaiBinariesMacosArm64: Copy = extractBonxaiBinariesTask("macos", "arm64")
val extractBonxaiBinariesAndroidX86_64: Copy = extractBonxaiBinariesTask("android", "x86_64")
val extractBonxaiBinariesAndroidArm64V8a: Copy = extractBonxaiBinariesTask("android", "arm64-v8a")
val extractBonxaiBinariesAndroidArmEabiV7a: Copy = extractBonxaiBinariesTask("android", "armeabi-v7a")
val extractBonxaiBinariesIosOs64: Copy = extractBonxaiBinariesTask("ios", "os64")
val extractBonxaiBinariesIosSimulator64: Copy = extractBonxaiBinariesTask("ios", "simulator64")
val extractBonxaiBinariesIosSimulatorArm64: Copy = extractBonxaiBinariesTask("ios", "simulatorarm64")

val extractBonxaiBinaries: Task = tasks.create("extractBonxaiBinaries") {
    group = "bonxaiBinaries"
    dependsOn(extractBonxaiBinariesWindowsX64)
    dependsOn(extractBonxaiBinariesLinuxX64)
    dependsOn(extractBonxaiBinariesLinuxArm64)
    dependsOn(extractBonxaiBinariesMacosX64)
    dependsOn(extractBonxaiBinariesMacosArm64)
    dependsOn(extractBonxaiBinariesAndroidX86_64)
    dependsOn(extractBonxaiBinariesAndroidArm64V8a)
    dependsOn(extractBonxaiBinariesAndroidArmEabiV7a)
    dependsOn(extractBonxaiBinariesIosOs64)
    dependsOn(extractBonxaiBinariesIosSimulator64)
    dependsOn(extractBonxaiBinariesIosSimulatorArm64)
}

val downloadBonxaiHeaders: Exec = tasks.create<Exec>("downloadBonxaiHeaders") {
    group = "bonxaiHeaders"
    dependsOn(ensureBuildDirectory)
    workingDir = layout.buildDirectory.get().asFile
    commandLine(
        "git", "clone", "--branch", libs.versions.bonxai.get(), "--single-branch",
        "https://github.com/karmakrafts/Bonxai", "bonxai/headers"
    )
    onlyIf { (layout.buildDirectory / "bonxai" / "headers").notExists() }
}

val updateBonxaiHeaders: Exec = tasks.create<Exec>("updateBonxaiHeaders") {
    group = "bonxaiHeaders"
    dependsOn(downloadBonxaiHeaders)
    workingDir = (layout.buildDirectory / "bonxai" / "headers").toFile()
    commandLine("git", "pull", "--force")
    onlyIf { (layout.buildDirectory / "bonxai" / "headers").exists() }
}

kotlin {
    listOf(
        mingwX64(), linuxX64(), linuxArm64(), macosX64(), macosArm64(), androidNativeArm32(), androidNativeArm64(),
        androidNativeX64(), iosX64(), iosArm64(), iosSimulatorArm64()
    ).forEach {
        it.compilations.getByName("main") {
            cinterops {
                val bonxai by creating {
                    tasks.getByName(interopProcessingTaskName) {
                        dependsOn(updateBonxaiHeaders)
                        dependsOn(extractBonxaiBinaries)
                    }
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.io.core)
                implementation(libs.kotlinx.io.bytestring)
            }
        }
    }
}

val dokkaJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks {
    dokkaHtml {
        dokkaSourceSets.create("main") {
            reportUndocumented = false
            jdkVersion = java.toolchain.languageVersion.get().asInt()
            noAndroidSdkLink = true
            externalDocumentationLink("https://docs.karmakrafts.dev${rootProject.name}")
        }
    }
    System.getProperty("publishDocs.root")?.let { docsDir ->
        create<Copy>("publishDocs") {
            mustRunAfter(dokkaJar)
            from(zipTree(dokkaJar.get().outputs.files.first()))
            into(docsDir)
        }
    }
}

publishing {
    System.getenv("CI_API_V4_URL")?.let { apiUrl ->
        repositories {
            maven {
                url = uri("$apiUrl/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create("header", HttpHeaderAuthentication::class)
                }
            }
        }
    }
    publications.configureEach {
        if (this is MavenPublication) {
            artifact(dokkaJar)
            pom {
                name = project.name
                description = "Multiplatform bindings for the Bonxai sparse voxel grid library on Linux, Windows and macOS."
                url = System.getenv("CI_PROJECT_URL")
                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                developers {
                    developer {
                        id = "kitsunealex"
                        name = "KitsuneAlex"
                        url = "https://git.karmakrafts.dev/KitsuneAlex"
                    }
                }
                scm {
                    url = this@pom.url
                }
            }
        }
    }
}
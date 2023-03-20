import net.ltgt.gradle.errorprone.errorprone

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("com.diffplug.spotless") version "6.15.0"
    id("net.ltgt.errorprone") version "3.0.1"
}

repositories {
    mavenCentral()
}

group = "org.zaproxy.gradle"
version = "0.1.0-SNAPSHOT"

val functionalTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
    runtimeClasspath += output + compileClasspath
}

dependencies {
    compileOnly("com.diffplug.spotless:spotless-plugin-gradle:6.15.0")
    implementation("org.apache.commons:commons-configuration2:2.8.0")
    "errorprone"("com.google.errorprone:error_prone_core:2.18.0")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    val jupiterVersion = "5.9.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    "functionalTestImplementation"("org.apiguardian:apiguardian-api:1.1.2")
    "functionalTestImplementation"("com.diffplug.spotless:spotless-plugin-gradle:6.15.0")
}

java {
    val javaVersion = JavaVersion.VERSION_11
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("-Xlint:all", "-Xlint:-path", "-Xlint:-options", "-Werror")
    options.errorprone {
        error(
            "MissingOverride",
            "WildcardImport",
        )
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    mustRunAfter(tasks.test)
    dependsOn(createFunctionalTestClasspathManifest)
}

val createFunctionalTestClasspathManifest = tasks.register<Task>("createFunctionalTestClasspathManifest") {
    description = "Creates a manifest file with the plugin classpath."
    group = "build"
    val outputDir = file("$buildDir/resources/functionalTest")
    inputs.files(functionalTest.runtimeClasspath)
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        file("$outputDir/pluginClasspath.txt").writeText(
            functionalTest.runtimeClasspath.joinToString("\n"),
        )
    }
}

tasks.check {
    dependsOn(functionalTestTask)
}

spotless {
    java {
        licenseHeaderFile("gradle/spotless/license.java")
        googleJavaFormat("1.7").aosp()
    }

    kotlin {
        ktlint()
    }

    kotlinGradle {
        ktlint()
    }
}

gradlePlugin {
    website.set("https://github.com/zaproxy/gradle-plugin-common")
    vcsUrl.set("https://github.com/zaproxy/gradle-plugin-common.git")
    plugins {
        create("zapCommon") {
            id = "org.zaproxy.common"
            implementationClass = "org.zaproxy.gradle.common.CommonPlugin"
            displayName = "Plugin for common ZAP build-related configs and tasks"
            description = "A Gradle plugin for common ZAP build-related configs and tasks."
            tags.set(listOf("zap", "zaproxy"))
        }
    }
    testSourceSets(functionalTest)
}
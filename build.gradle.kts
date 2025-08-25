plugins {
    id("java")
}

group = "blum"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// ---- Compilation du launcher Rust ----
val buildRustLauncher by tasks.registering(Exec::class) {
    workingDir = file("launcher")
    commandLine("cargo", "build", "--release")
}

// ---- Copier le launcher Rust ----
val copyRustLauncher by tasks.registering(Copy::class) {
    dependsOn(buildRustLauncher)
    val os = org.gradle.internal.os.OperatingSystem.current()
    val rustBinaryName = if (os.isWindows) "blumcore.exe" else "blumcore"

    from("launcher/target/release") {
        include(rustBinaryName)
    }
    into("$rootDir/output/bin")
}

// ---- Compiler le module core en app.jar ----
val buildCoreJar by tasks.registering(Jar::class) {
    dependsOn(":core:classes") // compiler uniquement le module core
    archiveFileName.set("app.jar")
    destinationDirectory.set(file("$rootDir/output/libs"))
    from(project(":core").sourceSets.main.get().output.classesDirs)
    from(project(":core").sourceSets.main.get().output.resourcesDir)
}

// ---- Copier les dépendances runtime du module core dans libs/ séparément ----
val copyCoreDependencies by tasks.registering(Copy::class) {
    dependsOn(buildCoreJar)
    from(project(":core").configurations.runtimeClasspath) {
        exclude { it.name == "app.jar" }
    }
    into("$rootDir/output/libs")
}

// ---- Branche le tout sur assemble/build ----
tasks.named("assemble") {
    dependsOn(buildCoreJar, copyCoreDependencies, copyRustLauncher)
}

tasks.named("build") {
    dependsOn(buildCoreJar, copyCoreDependencies, copyRustLauncher)
}

// Nettoyage
tasks.named("clean") {
    doFirst {
        delete("$rootDir/output")
        delete("launcher/target")
    }
}

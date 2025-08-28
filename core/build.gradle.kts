plugins {
    id("java")
}

group = "blum"
version = "1.0.25"

repositories {
    mavenCentral()
}

val jettyVersion = "11.0.25"
val joptVersion = "4.7"
val gsonVersion = "2.10.1"
val lombokVersion = "1.18.38"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.19.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.19.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    // Lombok
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    // Jetty serveur WebSocket
    implementation("org.eclipse.jetty:jetty-server:${jettyVersion}")
    implementation("org.eclipse.jetty.websocket:websocket-jetty-server:${jettyVersion}")

    implementation("com.typesafe:config:1.4.3")

    //Logging
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // Gson
    implementation("com.google.code.gson:gson:${gsonVersion}")

    implementation("net.sf.jopt-simple:jopt-simple:${joptVersion}")

    implementation("org.xerial:sqlite-jdbc:3.46.0.0")

    implementation("io.github.classgraph:classgraph:4.8.126")

    implementation(project(":api"))
}

tasks.test {
    useJUnitPlatform()
}
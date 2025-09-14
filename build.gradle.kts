plugins {
    id("java")
}

group = "dev.fluffix.plotmenu"
version = version

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation ("org.mongodb:mongo-java-driver:3.12.14")

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
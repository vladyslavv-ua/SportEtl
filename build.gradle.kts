plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.dataframe") version "2.4.21"
}

group = "io.vladyslavvua"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.datetime)

    implementation(libs.dataframe.csv)
    implementation(libs.dataframe.json)
    implementation(libs.dataframe.excel)
    implementation(libs.dataframe.core)

    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.core)
    implementation(libs.sqlite)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(26)
}

tasks.test {
    useJUnitPlatform()
}
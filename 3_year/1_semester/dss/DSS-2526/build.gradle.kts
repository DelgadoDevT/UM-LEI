plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")
}

application {
    mainClass.set("dss.Application")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    if (System.getProperty("console") == "plain") {
        setProperty("console", "plain")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            charSet = "UTF-8"
            windowTitle = "RestaurantChain DSS - API Documentation"
            docTitle = "RestaurantChain DSS Management System"
            header = "<b>RestaurantChain DSS</b>"
            bottom = "Copyright © 2025 Group 8 - DSS Course. All rights reserved."
            overview = "src/main/java/overview.html"
            addBooleanOption("Xdoclint:none", true)
            addStringOption("quiet")
            links("https://docs.oracle.com/en/java/javase/21/docs/api/")
        }
    }
}


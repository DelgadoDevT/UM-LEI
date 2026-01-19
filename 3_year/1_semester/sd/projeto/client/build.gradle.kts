plugins {
    id("java")
    id("application")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":middleware"))
}

application {
    mainClass.set("sd.client.UserInterface")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
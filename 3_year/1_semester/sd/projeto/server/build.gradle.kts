plugins {
    id("java")
    id("application")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":middleware"))
}

application {
    mainClass.set("sd.server.ServerMain")
}

tasks.register<Delete>("cleanData") {
    group = "application"
    val dataDir = file("data")
    if (dataDir.exists()) {
        delete(dataDir.listFiles())
    }
    doLast {
        println("✔ Conteúdo de 'data' eliminado.")
    }
}
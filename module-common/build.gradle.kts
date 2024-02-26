import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
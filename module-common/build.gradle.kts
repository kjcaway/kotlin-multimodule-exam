dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // For Jakarta Bean Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.persistence:jakarta.persistence-api:3.0.0")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
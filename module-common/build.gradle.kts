dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // For Jakarta Bean Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.persistence:jakarta.persistence-api:3.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

tasks.test {
    enabled = true
    useJUnitPlatform {
        includeTags("unit") // if use like that, only execute class with @Tag.
    }
    testLogging {
        events("passed", "skipped", "failed") // only logging pass, skipped and failed events
    }
}
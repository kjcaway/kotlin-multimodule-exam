val projectGroup: String by project
val applicationVersion: String by project

group = projectGroup
version = applicationVersion

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    implementation(project(":module-common"))

    /** Springboot **/
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    /** Kotlin **/
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /** Prometheus **/
    implementation("io.micrometer:micrometer-registry-prometheus")

    /** Test **/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.jar {
    enabled = true
}

tasks.test {
    enabled = true
    useJUnitPlatform {
        includeTags("unit", "api") // if use like that, only execute class with @Tag.
    }
    testLogging {
        events("passed", "skipped", "failed") // only logging pass, skipped and failed events
    }
}
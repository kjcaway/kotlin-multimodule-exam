import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("mysql:mysql-connector-java:8.0.33")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
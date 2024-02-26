import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val projectGroup: String by project
val applicationVersion: String by project

group = projectGroup
version = applicationVersion

dependencies {
    implementation(project(":module-common"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.jar {
    enabled = true
}
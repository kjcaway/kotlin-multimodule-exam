import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val projectGroup: String by project
val applicationVersion: String by project

group = projectGroup
version = applicationVersion

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-domain"))

    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /* Mysql */
    implementation("mysql:mysql-connector-java:8.0.28")

    /* Test Containers*/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:mysql:1.17.6")


}

tasks.jar {
    enabled = true
}

tasks.test {
    enabled = false
}
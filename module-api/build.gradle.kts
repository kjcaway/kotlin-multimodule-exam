val projectGroup: String by project
val applicationVersion: String by project

group = projectGroup
version = applicationVersion

dependencies {
    implementation(project(":module-common"))
    implementation(project(":module-domain"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    /* Mysql */
    implementation("mysql:mysql-connector-java:8.0.28")

    /* Neo4j */
    implementation("org.neo4j.driver:neo4j-java-driver:5.22.0")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")


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
    enabled = true
    useJUnitPlatform {
        includeTags("api") // if use like that, only execute class with @Tag.
    }
    testLogging {
        events("passed", "skipped", "failed") // only logging pass, skipped and failed events
    }
}
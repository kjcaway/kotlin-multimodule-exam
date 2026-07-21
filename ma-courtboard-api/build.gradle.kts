plugins {
    jacoco
    id("pl.allegro.tech.build.axion-release") version "1.18.16"
}

val projectGroup: String by project

scmVersion {
    tag {
        prefix.set("courtboard-v")
        versionSeparator.set("")
    }
    versionIncrementer("incrementMinor")
    unshallowRepoOnCI.set(true)
}

group = projectGroup
version = scmVersion.version

jacoco {
    toolVersion = "0.8.11"
}

configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

springBoot {
    buildInfo {
        properties {
            additional.set(mapOf("module" to "ma-courtboard-api"))
        }
    }
}

dependencies {
    implementation(project(":module-common"))

    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // runtimeOnly("com.mysql:mysql-connector-j")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // google id token verifier
    implementation("com.google.api-client:google-api-client:2.7.0")

    // password encoder
    implementation("org.mindrot:jbcrypt:0.4")

    // html sanitizer (xss prevention for rich text)
    implementation("org.jsoup:jsoup:1.17.2")

    // image thumbnail (avatar resize)
    implementation("net.coobird:thumbnailator:0.4.20")

    // casbin
    implementation("org.casbin:casbin-spring-boot-starter:1.9.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.jar {
    enabled = true
}

tasks.test {
    enabled = true
    useJUnitPlatform()

    // 테스트 종료 후 커버리지 리포트 생성 + 콘솔 출력
    finalizedBy(tasks.jacocoTestReport)

    testLogging {
        // 각 테스트 케이스의 진행/결과를 콘솔에 출력
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    // 테스트 스위트 종료 시 요약(성공/실패/스킵 개수) 출력
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // 루트 스위트에서만 요약 출력
                println(
                    "\nTest result: ${result.resultType} " +
                        "(${result.testCount} tests, " +
                        "${result.successfulTestCount} passed, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped)"
                )
            }
        }
    })
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)   // 콘솔 출력용 파싱 대상
        html.required.set(true)  // build/reports/jacoco/test/html/index.html
        csv.required.set(false)
    }

    // 커버리지 측정에서 제외: DTO / config / *Application
    val coverageExcludes = listOf(
        "**/dto/**",
        "**/config/**",
        "**/*Application*",
    )
    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) { exclude(coverageExcludes) }
        })
    )

    // 리포트 생성 후 XML을 파싱해 커버리지 요약을 콘솔에 출력
    doLast {
        val reportFile = reports.xml.outputLocation.get().asFile
        if (!reportFile.exists()) {
            logger.warn("JaCoCo XML report not found: ${reportFile.path}")
            return@doLast
        }

        val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
            // JaCoCo 리포트의 외부 DTD 로딩 비활성화(오프라인/파싱 오류 방지)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            isValidating = false
        }.newDocumentBuilder().parse(reportFile)

        val root = doc.documentElement

        fun coverage(type: String): String {
            val children = root.childNodes
            for (i in 0 until children.length) {
                val node = children.item(i)
                if (node is org.w3c.dom.Element &&
                    node.tagName == "counter" &&
                    node.getAttribute("type") == type
                ) {
                    val missed = node.getAttribute("missed").toLong()
                    val covered = node.getAttribute("covered").toLong()
                    val total = missed + covered
                    val pct = if (total == 0L) 0.0 else covered.toDouble() / total * 100
                    return "%5.1f%%  (%d/%d)".format(pct, covered, total)
                }
            }
            return "  N/A"
        }

        println("\n=== Test Coverage (JaCoCo) ===")
        println("Instructions : ${coverage("INSTRUCTION")}")
        println("Branches     : ${coverage("BRANCH")}")
        println("Lines        : ${coverage("LINE")}")
        println("Methods      : ${coverage("METHOD")}")
        println("Classes      : ${coverage("CLASS")}")
        println("==============================")
        println("HTML report  : ${reports.html.outputLocation.get().asFile.path}/index.html")
    }
}
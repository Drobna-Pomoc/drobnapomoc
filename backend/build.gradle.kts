import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.26.0"
}

group = "com.drobnapomoc"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val apiDir = layout.projectDirectory.dir("../api")
val generatedApiDir = layout.buildDirectory.dir("generated/openapi")

val apiSpecs = apiDir.asFileTree.matching {
	include("*.yaml")
	exclude(".*", "common.yaml")
}

val generateApi = tasks.register("generateApi")

apiSpecs.forEach { spec ->
	val task = tasks.register<GenerateTask>("generateApi_${spec.nameWithoutExtension}") {
		generatorName.set("spring")
		inputSpec.set(spec.path)
		inputs.files(apiDir.asFileTree.matching { include("*.yaml") })
		outputDir.set(generatedApiDir.get().asFile.path)
		apiPackage.set("com.drobnapomoc.drobnapomoc.api")
		modelPackage.set("com.drobnapomoc.drobnapomoc.api.model")
		modelNameSuffix.set("Rest")
		globalProperties.set(mapOf("apis" to "", "models" to "", "supportingFiles" to "none"))
		configOptions.set(
			mapOf(
				"interfaceOnly" to "true",
				"skipDefaultInterface" to "true",
				"useSpringBoot4" to "true",
				"useJackson3" to "true",
				"useTags" to "true",
				"includeHttpRequestContext" to "true",
				"requestMappingMode" to "api_interface",
				"documentationProvider" to "none",
				"openApiNullable" to "false",
				"containerDefaultToNull" to "true",
			)
		)
	}
	generateApi { dependsOn(task) }
}

sourceSets {
	main {
		java.srcDir(generatedApiDir.map { it.dir("src/main/java") })
	}
}

tasks.compileJava {
	dependsOn(generateApi)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

package com.drobnapomoc.drobnapomoc;

import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JDBC slice test against the Testcontainers PostgreSQL with Flyway migrations applied.
 * Each test runs in a transaction that is rolled back afterwards.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JdbcTest
@Import(TestcontainersConfiguration.class)
public @interface RepositoryTest {
}

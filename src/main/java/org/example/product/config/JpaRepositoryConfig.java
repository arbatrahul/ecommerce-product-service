package org.example.product.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.data.jpa.repositories.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "org.example.product.repository.jpa")
public class JpaRepositoryConfig {
}


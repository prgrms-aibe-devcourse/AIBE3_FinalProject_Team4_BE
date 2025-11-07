package com.back.global.config.elasticsearch;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.back.domain.blog.blogdoc.repository")
//@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true")
public class ElasticsearchConfig {
}


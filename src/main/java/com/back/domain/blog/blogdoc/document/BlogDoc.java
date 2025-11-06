package com.back.domain.blog.blogdoc.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "app1_blogs", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/mappings.json")
@Getter
@Setter
@Builder
public class BlogDoc {
    @Id
    private String id;
    private String title;
    private String content;
}

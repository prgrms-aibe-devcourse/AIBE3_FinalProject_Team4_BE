package com.back.domain.blog.blogdoc.document;

import com.back.domain.blog.blog.entity.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Document(indexName = "app1_blogs", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/blog-mappings.json")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlogDoc {
    @Id
    private Long id;
    private Long userId;
    @Field(type = FieldType.Keyword)
    private String userName;

    private String title;
    private String content;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Keyword)
    private List<String> hashtagName;

    @Field(type = FieldType.Keyword)
    private BlogStatus status;

    private long viewCount;
    private long likeCount;
    private long bookmarkCount;
    
    private String createdAt;
    private String modifiedAt;

    @Field(type = FieldType.Text)
    private String searchText;
    @Field(type = FieldType.Keyword)
    private List<Long> followerIds;

    @Transient
    private Object[] sortValues;

}

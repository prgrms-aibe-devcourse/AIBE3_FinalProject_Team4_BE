package com.back.domain.blog.blogdoc.document;

import com.back.domain.blog.hashtag.BlogHashtag;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "app1_blogs", createIndex = true)
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/mappings.json")
@Getter
@Setter
@Builder
public class BlogDoc {
    @Id
    private String id;
    private Long userId;

    private String title;
    private String content;

    @Field(type = FieldType.Keyword)
    private String userName;


    @Field(type = FieldType.Keyword)
    private String thumbnailImage;

    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private List<BlogHashtag> hashtags;

    @Field(type = FieldType.Keyword)
    private String status;

    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer commentCount;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Text)
    private String searchText;
    @Field(type = FieldType.Keyword)
    private List<Long> followerIds;

    @Transient
    private Object[] sortValues;

}

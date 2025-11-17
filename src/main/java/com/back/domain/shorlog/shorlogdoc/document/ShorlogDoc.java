package com.back.domain.shorlog.shorlogdoc.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "app1_shorlogs")
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/mappings.json")
@Getter
@Setter
@Builder
public class ShorlogDoc {

    @Id
    private String id;

    private Long userId;

    @Field(type = FieldType.Keyword)
    private String nickname;

    @Field(type = FieldType.Keyword)
    private String profileImgUrl;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String content;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Keyword)
    private List<String> hashtags;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer popularityScore; // 인기순 정렬에 사용

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;
}


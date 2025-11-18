package com.back.domain.shorlog.shorlogdoc.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
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

    /**
     * Elasticsearch에는 절대 시간(UTC)을 기준으로 저장하기 위해 Instant 타입을 사용한다.
     * <p>
     * 주의:
     * - DB(LocalDateTime)는 시간대 정보가 없으므로 저장 시 KST(Asia/Seoul) 기준으로 UTC 변환해야 한다.
     * - API 응답 등 사용자에게 출력 시에는 저장된 Instant 값을
     *   instant.atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
     *   형태로 변환하여 한국 시간(LocalDateTime)으로 반환해야 한다.
     */
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;
}

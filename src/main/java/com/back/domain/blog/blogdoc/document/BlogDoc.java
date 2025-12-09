package com.back.domain.blog.blogdoc.document;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Document(indexName = "app1_blogs", createIndex = true,
        writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/blog-mappings.json")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BlogDoc {
    @Id
    private Long id;
    private Long userId;
    @Field(type = FieldType.Keyword)
    private String userNickname;
    @Field(type = FieldType.Keyword)
    private String profileImgUrl;
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
    @Field(type = FieldType.Date)
    private Instant createdAt;
    @Field(type = FieldType.Date)
    private Instant modifiedAt;

    public static BlogDoc from(Blog blog) {
        List<String> hashtagNames = blog.getBlogHashtags().stream()
                .map(bh -> bh.getHashtag().getName())
                .toList();
        Instant createdAt = blog.getCreatedAt() == null ? null
                : blog.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant();

        Instant modifiedAt = blog.getModifiedAt() == null ? null
                : blog.getModifiedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant();

        return new BlogDoc(
                blog.getId(),
                blog.getUser().getId(),
                blog.getUser().getNickname(),
                blog.getUser().getProfileImgUrl(),
                blog.getTitle(),
                blog.getContent(),
                blog.getThumbnailUrl(),
                hashtagNames,
                blog.getStatus(),
                blog.getViewCount(),
                blog.getLikeCount(),
                blog.getBookmarkCount(),
                createdAt,
                modifiedAt
        );
    }

    public void changeUserNickname(String newNickname) {
        this.userNickname = newNickname;
    }

    public void changeProfileImgUrl(String newProfileImgUrl) {
        this.profileImgUrl = newProfileImgUrl;
    }
}
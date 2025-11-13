package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/data/test.sql")
@Transactional
@ActiveProfiles("test")
public class BlogControllerTest {
    @Autowired
    private MockMvc mockMvc;
    private String token;

    @BeforeEach
    void setup() throws Exception {
        // given
        String loginJson = """
                {
                  "username": "user1",
                  "password": "1234"
                }
                """;

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        token = JsonPath.read(responseBody, "$.data.accessToken");
    }

    @Test
    @DisplayName("블로그 글 작성 테스트")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "customSecurityUserService")
    void writeBlogTest() throws Exception {

        mockMvc.perform(
                        post("/api/v1/blogs") // 블로그 생성 API 엔드포인트 가정
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {
                                          "title": "테스트 블로그 제목",
                                          "content": "테스트 블로그 내용입니다.",
                                          "hashtagIds": [1],
                                          "status": "PUBLISHED"
                                        }
                                        """)

                )
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(ApiV1BlogController.class))
                .andExpect(jsonPath("resultCode").value("201-1"))
                .andExpect(jsonPath("msg").value("블로그 글 작성이 완료되었습니다."));
    }

    @Test
    @DisplayName("블로그 글 단건 조회")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "customSecurityUserService")
    void getBlogItemTest() throws Exception {

        BlogWriteReqDto requestDto = new BlogWriteReqDto(
                "테스트 블로그 제목",
                "테스트 블로그 내용입니다.",
                List.of(),
                BlogStatus.PUBLISHED,
                null
        );
        Long blogId = 100L;

        mockMvc.perform(
                        get("/api/v1/blogs/" + blogId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1BlogController.class))
                .andExpect(jsonPath("resultCode").value("200-2"))
                .andExpect(jsonPath("msg").value("블로그 글 조회가 완료되었습니다."));
    }

    @Test
    @DisplayName("블로그 글 수정")
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "customSecurityUserService")
    void modifyBlogTest() throws Exception {
        BlogWriteReqDto requestDto = new BlogWriteReqDto(
                "테스트 블로그 제목",
                "테스트 블로그 내용입니다.",
                List.of(),
                BlogStatus.PUBLISHED,
                null
        );
        Long blogId = 100L;

        mockMvc.perform(
                        put("/api/v1/blogs/" + blogId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {
                                          "title": "수정된 블로그 제목",
                                          "content": "수정된 블로그 내용입니다.",
                                          "hashtagIds": [1],
                                          "status": "DRAFT",
                                          "thumbnailUrl": null
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1BlogController.class))
                .andExpect(jsonPath("resultCode").value("200-3"))
                .andExpect(jsonPath("msg").value("블로그 글 수정이 완료되었습니다."));
    }
}
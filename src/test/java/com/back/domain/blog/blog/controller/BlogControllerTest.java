package com.back.domain.blog.blog.controller;

import com.back.domain.blog.blog.dto.BlogWriteReqDto;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.user.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BlogControllerTest {
    Long mockUserId = 1L;
    Long mockUserEmail = 1L;
    @Autowired
    private BlogService blogService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("블로그 글 작성 테스트")
    @WithUserDetails(value = "user1@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void writeBlogTest() throws Exception {

        mockMvc.perform(
                        post("/api/v1/blogs") // 블로그 생성 API 엔드포인트 가정
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-User-ID", "1")
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
    void getBlogItemTest() throws Exception {

        BlogWriteReqDto requestDto = new BlogWriteReqDto(
                "테스트 블로그 제목",
                "테스트 블로그 내용입니다.",
                List.of(),
                BlogStatus.PUBLISHED,
                null
        );
        Long blogId = 1L;

        mockMvc.perform(
                        get("/api/v1/blogs/" + blogId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1BlogController.class))
                .andExpect(jsonPath("resultCode").value("200-2"))
                .andExpect(jsonPath("msg").value("블로그 글 조회가 완료되었습니다."));
    }

    @Test
    @DisplayName("블로그 글 수정")
    @WithUserDetails(value = "user1@test.com", userDetailsServiceBeanName = "customUserDetailsService")
    void modifyBlogTest() throws Exception {
        BlogWriteReqDto requestDto = new BlogWriteReqDto(
                "테스트 블로그 제목",
                "테스트 블로그 내용입니다.",
                List.of(),
                BlogStatus.PUBLISHED,
                null
        );
        Long blogId = 1L;

        mockMvc.perform(
                        put("/api/v1/blogs/" + blogId)
                                .contentType(MediaType.APPLICATION_JSON)
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

    @Test
    @DisplayName("블로그 글 삭제")
    void deleteBlogTest() throws Exception {
        BlogWriteReqDto requestDto = new BlogWriteReqDto(
                "테스트 블로그 제목",
                "테스트 블로그 내용입니다.",
                List.of(),
                BlogStatus.PUBLISHED,
                null
        );
        Long blogId = 1L;
        mockMvc.perform(
                        delete("/api/v1/blogs/" + blogId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ApiV1BlogController.class))
                .andExpect(jsonPath("resultCode").value("200-4"))
                .andExpect(jsonPath("msg").value("블로그 글 삭제가 완료되었습니다."));
    }
}

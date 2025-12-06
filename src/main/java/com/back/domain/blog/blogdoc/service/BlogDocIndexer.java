package com.back.domain.blog.blogdoc.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogDocIndexer {
    private final BlogDocRepository blogDocRepository;
    private final BlogRepository blogRepository;

    @Transactional
    public void index(Long blogId) {
        Blog blog = blogRepository.findForIndexingById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (blog.getStatus() != BlogStatus.PUBLISHED) {
            blogDocRepository.deleteById(blogId);
            return;
        }
        BlogDoc doc = BlogDoc.from(blog);
        blogDocRepository.save(doc);
    }

    // 2) 유저 프로필 변경에 따른 부분 업데이트
    @Transactional
    public void updateUserProfileInBlogs(Long userId, String newNickname, String newProfileImageUrl) {
        List<BlogDoc> docs = blogDocRepository.findByUserId(userId);
        if (docs.isEmpty()) {
            return; // 블로그 없으면 조용히 리턴
        }
        for (BlogDoc doc : docs) {
            doc.changeUserNickname(newNickname);
            doc.changeProfileImgUrl(newProfileImageUrl);
        }
        blogDocRepository.saveAll(docs);
    }

    public void delete(Long blogId) {
        blogDocRepository.deleteById(blogId);
    }
}
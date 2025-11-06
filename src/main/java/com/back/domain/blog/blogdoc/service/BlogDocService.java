package com.back.domain.blog.blogdoc.service;

import com.back.domain.blog.blogdoc.document.BlogDoc;
import com.back.domain.blog.blogdoc.repository.BlogDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogDocService {
    private final BlogDocRepository blogDocRepository;

    public BlogDoc write(String title, String content) {
        BlogDoc postDoc = BlogDoc.builder()
                .title(title)
                .content(content)
                .build();
        return blogDocRepository.save(postDoc);
    }

    public void truncate() {
        blogDocRepository.deleteAll();
    }

    public List<BlogDoc> searchByKeyword(String keyword) {
        return blogDocRepository.searchByKeyword(keyword);
    }
}

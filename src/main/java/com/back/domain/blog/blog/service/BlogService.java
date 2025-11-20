package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.service.BlogDocIndexer;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.service.BlogLikeService;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shared.hashtag.service.HashtagService;
import com.back.domain.shared.image.service.ImageLifecycleService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogLikeService blogLikeService;
    private final BlogBookmarkService blogBookmarkService;
    private final CommentsService commentsService;
    private final HashtagService hashtagService;
    private final ImageLifecycleService imageLifecycleService;
    private final BlogDocIndexer blogDocIndexer;

    public void truncate() {
        blogRepository.deleteAll();
    }

    @Transactional
    public BlogDetailDto findById(Long userId, Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        boolean liked = blogLikeService.isLiked(id, userId);
        boolean bookmarked = blogBookmarkService.isBookmarked(id, userId);
        List<CommentResponseDto> comments = commentsService.getCommentsByType(id, CommentsTargetType.BLOG);
        Map<Long, Long> commentCount = commentsService.getCommentCounts(List.of(id), CommentsTargetType.BLOG);

        return new BlogDetailDto(blog, liked, bookmarked, comments, commentCount.getOrDefault(id, 0L));
    }

    @Transactional
    public BlogWriteDto write(Long userId, BlogWriteReqDto reqBody) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        Blog blog = new Blog(user, reqBody.title(), reqBody.content(), BlogStatus.PUBLISHED);

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        blog.updateHashtags(hashtags);

        blog.publish();
        blog = blogRepository.save(blog);
        blogDocIndexer.index(blog);
        return new BlogWriteDto(blog);
    }

    @Transactional
    public BlogModifyDto modify(Long userId, Long blogId, BlogWriteReqDto reqBody) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blog.modify(reqBody);
        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        blog.updateHashtags(hashtags);

        blog.publish();
        blogDocIndexer.index(blog);
        return new BlogModifyDto(blog);
    }

    @Transactional
    public long increaseView(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        blog.increaseViewCount();
        return blog.getViewCount();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        imageLifecycleService.decrementReference(blog.getThumbnailUrl());
        blogRepository.delete(blog);
        blogDocIndexer.delete(id);
    }

    @Transactional
    public BlogWriteDto saveDraft(Long userId, @Valid BlogWriteReqDto reqbody) {
        Blog blog = createDraft(userId, reqbody);     // 존재하지 않으면 새로 생성
        updateDraft(blog, reqbody);

        return new BlogWriteDto(blog);
    }

    private Blog createDraft(Long userId, BlogWriteReqDto reqBody) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        Blog newBlog = Blog.create(user, reqBody.title(), reqBody.content(), BlogStatus.DRAFT);

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        newBlog.updateHashtags(hashtags);
        newBlog.unpublish();
        return blogRepository.save(newBlog);
    }

    private void updateDraft(Blog blog, BlogWriteReqDto req) {
        blog.modify(req);
        blog.unpublish();
    }

    public List<BlogDraftDto> findDraftsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        List<Blog> drafts = blogRepository.findByStatusAndUserId(BlogStatus.DRAFT, userId);
        return drafts.stream()
                .map(b -> new BlogDraftDto(b))
                .toList();
    }

    public List<BlogDto> findAllByMy(Long userId, BlogMySortType sortType) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        List<Blog> blogs = blogRepository
                .findAllByUserIdAndStatusWithSort(userId, BlogStatus.PUBLISHED, sortType);

        return blogs.stream()
                .map(b -> new BlogDto(b, false, false, 0)).toList();
    }

    public void deleteDraft(Long id, Long draftId) {
        Blog blog = blogRepository.findById(draftId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(id)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blogRepository.delete(blog);
        blogDocIndexer.delete(id);
    }
}
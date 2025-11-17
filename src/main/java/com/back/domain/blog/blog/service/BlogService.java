package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void truncate() {
        blogRepository.deleteAll();
    }

    public Page<BlogDto> findAll(Long userId, Pageable pageable) {
        Page<Blog> blogs = blogRepository.findAll(pageable);
        List<Long> blogIds = blogs.stream().map(Blog::getId).toList();

        // 좋아요/북마크 여부를 한 번에 조회 (N+1 해결)
        Set<Long> likedIds = blogLikeService.findLikedBlogIds(userId, blogIds);
        Set<Long> bookmarkedIds = blogBookmarkService.findBookmarkedBlogIds(userId, blogIds);
        // 댓글 수 batch 조회 (N+1 해결)
        Map<Long, Long> commentCounts = commentsService.getCommentCounts(blogIds, CommentsTargetType.BLOG);

        return blogs.map(blog -> new BlogDto(
                blog,
                likedIds.contains(blog.getId()),
                bookmarkedIds.contains(blog.getId()),
                commentCounts.getOrDefault(blog.getId(), 0L)
        ));
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
    public BlogWriteDto write(Long userId, BlogWriteReqDto reqBody, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        Blog blog = new Blog(user, reqBody.title(), reqBody.content(), reqBody.thumbnailUrl(), BlogStatus.PUBLISHED);

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        blog.updateHashtags(hashtags);

        blog.changeThumbnailUrl(thumbnailUrl);
        imageLifecycleService.incrementReference(thumbnailUrl);
        blog.publish();
        blog = blogRepository.save(blog);
        return new BlogWriteDto(blog);
    }

    @Transactional
    public BlogModifyDto modify(Long userId, Long blogId, BlogWriteReqDto reqBody, String thumbnailUrl) {
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

        blog.changeThumbnailUrl(thumbnailUrl);
        imageLifecycleService.incrementReference(thumbnailUrl);
        blog.publish();
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
    }

    @Transactional
    public BlogWriteDto saveDraft(Long userId, @Valid BlogWriteReqDto reqbody, String thumbnailUrl) {
        Blog blog = createDraft(userId, reqbody, thumbnailUrl);     // 존재하지 않으면 새로 생성
        updateDraft(blog, reqbody, thumbnailUrl);

        return new BlogWriteDto(blog);
    }

    private Blog createDraft(Long userId, BlogWriteReqDto reqBody, String thumbnailUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        Blog newBlog = Blog.create(user, reqBody.title(), reqBody.content(), thumbnailUrl, BlogStatus.DRAFT);

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        newBlog.updateHashtags(hashtags);
        newBlog.unpublish();
        return blogRepository.save(newBlog);
    }

    private void updateDraft(Blog blog, BlogWriteReqDto req, String thumbnailUrl) {
        blog.modify(req);
        blog.unpublish();
    }

    public List<BlogDraftDto> findDraftsByUserId(Long userId) {
        List<Blog> drafts = blogRepository.findByStatusAndUserId(BlogStatus.DRAFT, userId);
        return drafts.stream()
                .map(b -> new BlogDraftDto(b))
                .toList();
    }

    public List<BlogDto> findAllByUserId(Long userId) {
        List<Blog> blogs = blogRepository.findAllByUserIdAndStatus(userId, BlogStatus.PUBLISHED);

        return blogs.stream()
                .map(b -> new BlogDto(b,
                        false,
                        false,
                        0
                ))
                .toList();
    }

    public void deleteDraft(Long id, Long draftId) {
        Blog blog = blogRepository.findById(draftId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(id)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blogRepository.delete(blog);
    }
}
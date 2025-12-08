package com.back.domain.blog.blog.service;

import com.back.domain.blog.blog.dto.*;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.entity.BlogStatus;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.repository.BlogBookmarkQueryRepository;
import com.back.domain.blog.bookmark.service.BlogBookmarkService;
import com.back.domain.blog.like.service.BlogLikeService;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.domain.recommend.recentview.service.RecentViewService;
import com.back.domain.recommend.search.type.PostType;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shared.hashtag.service.HashtagService;
import com.back.domain.shared.image.service.ImageLifecycleService;
import com.back.domain.shared.link.repository.ShorlogBlogLinkRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final BlogBookmarkQueryRepository blogBookmarkQueryRepository;
    private final CommentsService commentsService;
    private final HashtagService hashtagService;
    private final ImageLifecycleService imageLifecycleService;
    private final ApplicationEventPublisher eventPublisher;

    private final ShorlogBlogLinkRepository shorlogBlogLinkRepository;
    private final RecentViewService recentViewService;

    @Transactional
    public BlogDetailDto findById(Long userId, Long blogId) {
        Blog blog = blogRepository.findDetailWithFiles(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (blog.getStatus() == BlogStatus.DRAFT &&
                (userId == null || !blog.getUser().getId().equals(userId))) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        boolean liked = (userId != null) && blogLikeService.isLiked(blogId, userId);
        boolean bookmarked = (userId != null) && blogBookmarkService.isBookmarked(blogId, userId);

        List<CommentResponseDto> comments = commentsService.getCommentsByType(blogId, CommentsTargetType.BLOG);
        Map<Long, Long> commentCount = commentsService.getCommentCounts(List.of(blogId), CommentsTargetType.BLOG);
        long linkedShorlogCount = shorlogBlogLinkRepository.countByBlogId(blogId);
        List<String> hashtagNames = blogRepository.findHashtagNamesByBlogId(blogId);
        return new BlogDetailDto(blog, hashtagNames, liked, bookmarked, comments, commentCount.getOrDefault(blogId, 0L), linkedShorlogCount);
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
        eventPublisher.publishEvent(new BlogIndexEvent(blog.getId()));
        return new BlogWriteDto(blog);
    }

    @Transactional
    public BlogWriteDto modify(Long userId, Long blogId, BlogWriteReqDto reqBody) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        blog.updateHashtags(hashtags);
        blog.modify(reqBody);

        eventPublisher.publishEvent(new BlogIndexEvent(blog.getId()));
        return new BlogWriteDto(blog);
    }

    @Transactional
    public long increaseView(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        blog.increaseViewCount();
        eventPublisher.publishEvent(new BlogIndexEvent(blog.getId()));
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

        if (blog.getStatus() == BlogStatus.PUBLISHED) {
            eventPublisher.publishEvent(new BlogIndexDeleteEvent(id));
            shorlogBlogLinkRepository.deleteByBlogId(id);
        }
        blogRepository.delete(blog);
    }

    @Transactional
    public BlogWriteDto createDraft(Long userId, BlogWriteReqDto reqBody) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));

        Blog newBlog = Blog.create(user, reqBody.title(), reqBody.content(), BlogStatus.DRAFT);

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        newBlog.updateHashtags(hashtags);
        newBlog.unpublish();
        blogRepository.save(newBlog);
        return new BlogWriteDto(newBlog);
    }

    @Transactional
    public BlogWriteDto updateDraft(Long userId, Long blogId, BlogWriteReqDto reqBody) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        blog.modify(reqBody);
        List<Hashtag> hashtags = hashtagService.findOrCreateAll(reqBody.hashtagNames());
        blog.updateHashtags(hashtags);
        blog.unpublish();

        blogRepository.save(blog);
        return new BlogWriteDto(blog);
    }

    public List<BlogDraftDto> findDraftsByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        List<Blog> drafts = blogRepository.findByStatusAndUserId(BlogStatus.DRAFT, userId);
        return drafts.stream()
                .map(BlogDraftDto::new)
                .toList();
    }

    public Page<BlogDto> findAllByMy(Long userId, BlogMySortType sortType, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.PERMISSION_DENIED));
        Page<Blog> blogs = blogRepository.findMyBlogs(userId, sortType, pageable);
        List<Long> blogIds = blogs.stream().map(Blog::getId).toList();
        final Map<Long, Long> commentCounts =
                commentsService.getCommentCounts(blogIds, CommentsTargetType.BLOG);
        return blogs.map(blog ->
                new BlogDto(blog, false, false, commentCounts.getOrDefault(blog.getId(), 0L))
        );
    }

    public Page<BlogDto> findAllByUserId(Long targetId, Long viewerId, BlogMySortType sortType, Pageable pageable) {
        Page<Blog> blogs = blogRepository.findByUserId(targetId, sortType, pageable);
        if (blogs.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Long> blogIds = blogs.stream().map(Blog::getId).toList();

        Set<Long> likedIds = viewerId != null
                ? blogLikeService.findLikedBlogIds(viewerId, blogIds) : Set.of();
        Set<Long> bookmarkedIds = viewerId != null
                ? blogBookmarkService.findBookmarkedBlogIds(viewerId, blogIds) : Set.of();
        Map<Long, Long> commentCounts =
                commentsService.getCommentCounts(blogIds, CommentsTargetType.BLOG);

        return blogs.map(blog ->
                new BlogDto(
                        blog,
                        likedIds.contains(blog.getId()),
                        bookmarkedIds.contains(blog.getId()),
                        commentCounts.getOrDefault(blog.getId(), 0L)
                )
        );
    }

    public Page<BlogDto> getMyBookmarkedBlogs(Long userId, BlogMySortType sortType, Pageable pageable) {
        Page<Blog> blogs =
                blogBookmarkQueryRepository.findBookmarkedBlogs(userId, sortType, pageable);
        if (blogs.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Long> blogIds = blogs.stream().map(Blog::getId).toList();
        final Set<Long> likedIds = blogLikeService.findLikedBlogIds(userId, blogIds);
        final Set<Long> bookmarkedIds = blogBookmarkService.findBookmarkedBlogIds(userId, blogIds);
        final Map<Long, Long> commentCounts = commentsService.getCommentCounts(blogIds, CommentsTargetType.BLOG);

        return blogs.map(blog ->
                new BlogDto(blog, likedIds.contains(blog.getId()), bookmarkedIds.contains(blog.getId()), commentCounts.getOrDefault(blog.getId(), 0L))
        );
    }

    public void view(String guestId, Long userId, Long id) {
        blogRepository.findById(id)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));

        recentViewService.addRecentViewPost(guestId, PostType.BLOG, id);

        if (userId != null && userId > 0) {
            recentViewService.mergeGuestHistoryToUser(guestId, userId, PostType.BLOG);
        }
    }
}
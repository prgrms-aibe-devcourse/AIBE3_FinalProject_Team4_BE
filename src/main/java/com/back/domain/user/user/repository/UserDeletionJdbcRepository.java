package com.back.domain.user.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserDeletionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void deleteUserCompletely(Long userId) {
        // 0) 메시지까지 전부 삭제: 탈퇴 유저가 속한 thread 통째로 삭제
        List<Long> threadIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT message_thread_id FROM message_participant WHERE user_id = ?",
                Long.class,
                userId
        );

        if (!threadIds.isEmpty()) {
            String inSql = threadIds.stream().map(id -> "?").collect(Collectors.joining(","));
            Object[] args = threadIds.toArray();

            // FK 순서: message -> message_participant -> message_thread
            jdbcTemplate.update("DELETE FROM message WHERE message_thread_id IN (" + inSql + ")", args);
            jdbcTemplate.update("DELETE FROM message_participant WHERE message_thread_id IN (" + inSql + ")", args);
            jdbcTemplate.update("DELETE FROM message_thread WHERE id IN (" + inSql + ")", args);
        }

        // 1) 팔로우
        jdbcTemplate.update("DELETE FROM follow WHERE from_user_id = ? OR to_user_id = ?", userId, userId);
        jdbcTemplate.update("DELETE FROM notification WHERE receiver_id = ? OR sender_id = ?", userId, userId);
        // 2) 유저가 누른 좋아요/북마크/반응
        jdbcTemplate.update("DELETE FROM blog_like WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM blog_bookmarks WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM shorlog_like WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM shorlog_bookmark WHERE user_id = ?", userId);

        // 댓글 좋아요 매핑(liked_user_ids가 userId인 것)
        jdbcTemplate.update("DELETE FROM comments_liked_user_ids WHERE liked_user_ids = ?", userId);

        // 3) 유저 작성 블로그 관련(자식/연결 테이블 → 본문)
        jdbcTemplate.update("""
                UPDATE images i
                JOIN blog_files bf ON i.id = bf.image_id
                JOIN blogs b ON bf.blog_id = b.id
                SET i.reference_count = i.reference_count - 1
                WHERE b.user_id = ? AND i.reference_count > 0
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM blog_files
                WHERE blog_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM blog_hashtags
                WHERE blog_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        // 블로그 글에 달린 타인의 좋아요/북마크까지 제거
        jdbcTemplate.update("""
                DELETE FROM blog_like
                WHERE blog_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM blog_bookmarks
                WHERE blog_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        // 4) 유저 작성 쇼로그 관련(연결 테이블 → 본문)
        jdbcTemplate.update("""
                DELETE FROM shorlog_blog_link
                WHERE shorlog_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM shorlog_hashtag
                WHERE shorlog_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        // 숏로그 이미지 참조 카운트 감소
        jdbcTemplate.update("""
            UPDATE images i
            JOIN shorlog_images si ON i.id = si.image_id
            JOIN shorlog s ON si.shorlog_id = s.id
            SET i.reference_count = i.reference_count - 1
            WHERE s.user_id = ? AND i.reference_count > 0
            """, userId);

        jdbcTemplate.update("""
                DELETE FROM shorlog_images
                WHERE shorlog_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM shorlog_like
                WHERE shorlog_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM shorlog_bookmark
                WHERE shorlog_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        // 5) 댓글 삭제 (FK: parent_id, + liked 매핑)
        // 5-1) 유저가 작성한 댓글에 딸린 liked 매핑 삭제
        jdbcTemplate.update("""
                DELETE cl FROM comments_liked_user_ids cl
                JOIN comments c ON c.id = cl.comments_id
                WHERE c.user_id = ?
                """, userId);

        // 5-2) 유저가 작성한 댓글의 자식댓글 먼저 삭제 (parent FK)
        jdbcTemplate.update("""
                DELETE c_child FROM comments c_child
                JOIN comments c_parent ON c_child.parent_id = c_parent.id
                WHERE c_parent.user_id = ?
                """, userId);

        // 5-3) 유저가 작성한 댓글 삭제
        jdbcTemplate.update("DELETE FROM comments WHERE user_id = ?", userId);

        // 5-4) 유저 블로그/쇼로그에 달린 댓글까지 전부 삭제(“관련 데이터 전부” 정책)
        jdbcTemplate.update("""
                DELETE cl FROM comments_liked_user_ids cl
                JOIN comments c ON c.id = cl.comments_id
                WHERE c.target_type='BLOG'
                  AND c.target_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        jdbcTemplate.update("""
                DELETE c_child FROM comments c_child
                JOIN comments c_parent ON c_child.parent_id = c_parent.id
                WHERE c_parent.target_type='BLOG'
                  AND c_parent.target_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM comments
                WHERE target_type='BLOG'
                  AND target_id IN (SELECT id FROM (SELECT id FROM blogs WHERE user_id = ?) b)
                """, userId);

        jdbcTemplate.update("""
                DELETE cl FROM comments_liked_user_ids cl
                JOIN comments c ON c.id = cl.comments_id
                WHERE c.target_type='SHORLOG'
                  AND c.target_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        jdbcTemplate.update("""
                DELETE c_child FROM comments c_child
                JOIN comments c_parent ON c_child.parent_id = c_parent.id
                WHERE c_parent.target_type='SHORLOG'
                  AND c_parent.target_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        jdbcTemplate.update("""
                DELETE FROM comments
                WHERE target_type='SHORLOG'
                  AND target_id IN (SELECT id FROM (SELECT id FROM shorlog WHERE user_id = ?) s)
                """, userId);

        // 6) 본문 삭제
        jdbcTemplate.update("DELETE FROM blogs WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM shorlog WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM shorlog_draft WHERE user_id = ?", userId);

        // 7) 로그성 데이터 (조회 기록은 남기고 viewer 익명화)
        jdbcTemplate.update("UPDATE content_view_history SET viewer_id = NULL WHERE viewer_id = ?", userId);
        jdbcTemplate.update("DELETE FROM search_history WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM model_usages WHERE user_id = ?", userId);

        // 8) 이미지(주의: 참조관계 다 끊은 뒤)
        jdbcTemplate.update("DELETE FROM images WHERE user_id = ?", userId);

        // 9) 마지막: 유저 삭제
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
    }
}

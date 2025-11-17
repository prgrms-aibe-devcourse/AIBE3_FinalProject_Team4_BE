package com.back.domain.user.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    private String email;           // 이메일
    @Column(unique = true)
    private String username;        // 가입 ID
    private String password;        // 비밀번호

    @Column(unique = true)
    private String nickname;        // 닉네임
    private String profileImgUrl;   // 프로필 이미지 URL
    private String bio;             // 간단 자기소개

    private long followersCount = 0L;   // 팔로워 수
    private long followingCount = 0L;   // 팔로잉 수
    private long likesCount = 0L;       // 좋아요 수

    private int shorlogsCount = 0;     // 쇼로그 수
    private int blogsCount = 0;        // 블로그 수
    private int shorlogBookmarksCount = 0; // 북마크한 쇼로그 수
    private int blogBookmarksCount = 0; // 북마크한 블로그 수

    private LocalDate dateOfBirth;       // 생년월일
    @Enumerated(EnumType.STRING)
    private Gender gender;          // 성별

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // 기본 권한 USER

    // 일반 가입용 생성자
    public User(String email, String username, String password, String nickname, LocalDate dateOfBirth, Gender gender) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.profileImgUrl = null;  //가입 시 기본 프로필 이미지 URL은 null
        this.bio = null;            //가입 시 기본 자기소개는 null
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    // OAuth2 가입용 생성자
    public User(String username, String profileImgUrl) {
        this.username = username;
        this.profileImgUrl = profileImgUrl;
        this.nickname = null;       // 가입 직후 nickname 설정
        this.dateOfBirth = null;    // 가입 직후 dateOfBirth 설정
        this.gender = null;         // 가입 직후 성별 설정
        this.bio = null;            // 추후 수정 가능
        this.email = null;          // OAuth2 사용자는 이메일 필요 없음
        this.password = null;       // OAuth2 사용자는 비밀번호 필요 없음
    }

    // OAuth2 로그인 시 사용자 정보 업데이트용 함수
    public void updateProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    // OAuth2 가입 완료용 함수
    public void completeOAuth2Join(String nickname, LocalDate dateOfBirth, Gender gender) {
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateProfile(String nickname, String bio, String profileImgUrl) {
        this.nickname = nickname;
        this.bio = bio;
        this.profileImgUrl = profileImgUrl;
    }
}
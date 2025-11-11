package com.back.domain.user.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity{

    private String email;           // 이메일
    @Column(unique = true)
    private String username;        // 가입 ID
    private String password;        // 비밀번호

    @Column(unique = true)
    private String nickname;        // 닉네임
    private String profileImgUrl;   // 프로필 이미지 URL
    private String bio;             // 간단 자기소개
    private LocalDate dateOfBirth;       // 생년월일
    @Enumerated(EnumType.STRING)
    private Gender gender;          // 성별

    // 추가 가입 정보까지 받는 생성자, 추후 확장 가능성 고려
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

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
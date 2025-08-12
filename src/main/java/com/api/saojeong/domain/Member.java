package com.api.saojeong.domain;


import com.api.saojeong.Member.dto.SignupRequestDto;
import com.api.saojeong.Member.dto.SignupResponseDto;
import com.api.saojeong.global.utill.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="MEMBERS")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="nickname", nullable=false, unique=true)
    private String nickname;

    @Column(name="memberId",nullable=false, unique=true)
    private String memberId;

    @Column(name="password", nullable=false)
    private String password;

    @Column(name="carnum",nullable = false, unique = true)
    private String carnum;

    @Column(name = "refresh_token")
    private String refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberRole> memberRoleList = new ArrayList<>();

    

    public void changeRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public static Member toMember(SignupRequestDto request, String encodedPassword,
                                  String refreshToken) {
        return Member.builder()
                .memberId(request.getMemberId())
                .password(encodedPassword)
                .carnum(request.getCarnumber())
                .refreshToken(refreshToken)
                .nickname(request.getName())
                .memberRoleList(new ArrayList<>())
                .build();

    }

    public static SignupResponseDto toSignupResponseDto(Member member) {
        return SignupResponseDto.builder()
                .id(member.getId())
                .createdAt(member.getCreatedAt())
                .build();
    }


    public void addMemberRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
        memberRole.setMember(this);
    }

}

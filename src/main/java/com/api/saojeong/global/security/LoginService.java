package com.api.saojeong.global.security;


import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        Member member = memberRepository.findByMemberId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디가 존재하지 않습니다."));

        // 권한을 Set<GrantedAuthority> 타입으로 설정
        Set<GrantedAuthority> authorities = member.getMemberRoleList()
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getRole().getAuthority().toString()))
                .collect(Collectors.toSet()); // Set<GrantedAuthority> 반환

        return User.builder()
                .username(member.getMemberId())
                .authorities(authorities)  // 권한 설정
                .password(member.getPassword())
                .build();
    }
}


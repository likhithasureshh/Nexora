package com.projects.distributed_lovable.account_service.service.impl;

import com.projects.distributed_lovable.account_service.entity.User;
import com.projects.distributed_lovable.account_service.repository.UserRepository;
import com.projects.distributed_lovable.common_lib.errors.ResourceNotFoundException;
import com.projects.distributed_lovable.common_lib.security.JwtUserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User",username));

        return new JwtUserPrinciple(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}

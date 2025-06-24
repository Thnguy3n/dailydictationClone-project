//package com.example.userservice.service.Impl;
//
//import com.example.userservice.entity.UserEntity;
//import com.example.userservice.model.UserPrincipal;
//import com.example.userservice.repository.UserRepository;
//import com.example.userservice.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class MyUserDetailsService implements UserDetailsService {
//    private final UserRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserEntity userEntity = userRepository.findByUsername(username);
//        if (userEntity == null) {
//            throw new UsernameNotFoundException("username not found");
//        }
//        return new UserPrincipal(userEntity);
//    }
//}

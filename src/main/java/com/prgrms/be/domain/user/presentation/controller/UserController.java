package com.prgrms.be.domain.user.presentation.controller;

import com.prgrms.be.domain.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    //테스트용 컨트롤러
//    @GetMapping("/jwt-test")
//    public String jwtTest() {
//        return "jwtTest 요청 성공";
//    }
}

package com.example.demo.controller.home;

import javax.servlet.http.HttpServletResponse;

import com.example.demo.base.response.BaseResponse;
import com.example.demo.config.jwt.JwtTokenProvider;
import com.example.demo.dto.auth.LoginDTO;
import com.example.demo.dto.auth.RegisterDTO;
import com.example.demo.dto.user.UserDTO;
import com.example.demo.service.user.CustomUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
public class HomeController {

    @Autowired
    private CustomUserService customUserService;

    @GetMapping(value = "/login")
    public BaseResponse login(@RequestBody UserDTO userDTO) {
        return customUserService.login(userDTO);
    }

    @PostMapping(value = "/register")
    public BaseResponse register(@RequestBody UserDTO registerDto) {

        return customUserService.registerUser(registerDto);
    }

    @GetMapping("/remove/{id}")
    public BaseResponse removeUser(@PathVariable("id") Integer userId) {
        System.out.println(userId);
        return customUserService.removeUser(userId);
    }

}

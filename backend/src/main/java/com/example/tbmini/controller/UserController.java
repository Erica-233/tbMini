package com.example.tbmini.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tbmini.domain.common.Result;
import com.example.tbmini.dto.PostVo;
import com.example.tbmini.dto.UpdateProfileRequest;
import com.example.tbmini.dto.UserProfileResponse;
import com.example.tbmini.security.UserPrincipal;
import com.example.tbmini.service.PostService;
import com.example.tbmini.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PostService postService;

    @GetMapping("/{username}")
    public Result<UserProfileResponse> getProfile(@PathVariable String username) {
        return Result.ok(userService.getProfileByUsername(username));
    }

    @PutMapping("/me")
    public Result<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.updateProfile(principal.getUserId(), req);
        return Result.ok();
    }

    @GetMapping("/{username}/posts")
    public Result<Page<PostVo>> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(postService.listUserPosts(username, page, size));
    }
}

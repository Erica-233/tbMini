package com.example.tbmini;

import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Role;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.UserMapper;
import com.example.tbmini.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class LikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private Long userId;
    private Long postId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname("User");
        user.setRole(Role.USER.name());
        user.setIsBanned(false);
        userMapper.insert(user);
        userId = user.getId();
        userToken = "Bearer " + jwtUtil.generateToken(userId, user.getEmail(), user.getRole());

        Board board = new Board();
        board.setName("技术交流");
        board.setSlug("tech");
        board.setDescription("技术讨论");
        board.setIsActive(true);
        boardMapper.insert(board);

        Post post = new Post();
        post.setBoardId(board.getId());
        post.setUserId(userId);
        post.setTitle("Test Post");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);
        postId = post.getId();
    }

    @Test
    void like_shouldIncreaseCount() throws Exception {
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    void likeTwice_shouldBeIdempotent() throws Exception {
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    void unlike_shouldDecreaseCount() throws Exception {
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    void unlikeTwice_shouldBeIdempotent() throws Exception {
        mockMvc.perform(delete("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/" + postId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    void guest_shouldSeeLikeCountButLikedFalse() throws Exception {
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    void listPosts_shouldIncludeLikeInfo() throws Exception {
        mockMvc.perform(post("/api/posts/" + postId + "/like")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.records[0].liked").value(true));
    }
}

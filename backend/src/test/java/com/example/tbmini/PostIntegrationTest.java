package com.example.tbmini;

import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Role;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
public class PostIntegrationTest {

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
    private com.example.tbmini.security.JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long adminId;
    private Long boardId;

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

        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setNickname("Admin");
        admin.setRole(Role.ADMIN.name());
        admin.setIsBanned(false);
        userMapper.insert(admin);
        adminId = admin.getId();
        adminToken = "Bearer " + jwtUtil.generateToken(adminId, admin.getEmail(), admin.getRole());

        Board board = new Board();
        board.setName("技术交流");
        board.setSlug("tech");
        board.setDescription("技术讨论");
        board.setIsActive(true);
        boardMapper.insert(board);
        boardId = board.getId();
    }

    @Test
    void createPost_shouldSucceed() throws Exception {
        String json = "{\"boardSlug\":\"tech\",\"title\":\"Hello\",\"bodyMd\":\"World\"}";
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        mockMvc.perform(multipart("/api/posts")
                        .file(dataPart)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Hello"))
                .andExpect(jsonPath("$.data.bodyMd").value("World"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void getPost_shouldReturnDetailWithImages() throws Exception {
        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle("Test Post");
        post.setBodyMd("Body");
        post.setContent("Body");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Test Post"))
                .andExpect(jsonPath("$.data.images").isArray());
    }

    @Test
    void updatePost_byAuthor_shouldSucceed() throws Exception {
        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle("Old Title");
        post.setBodyMd("Old Body");
        post.setContent("Old Body");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        String json = "{\"boardSlug\":\"tech\",\"title\":\"New Title\",\"bodyMd\":\"New Body\"}";
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        mockMvc.perform(multipart("/api/posts/" + post.getId())
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("New Title"));
    }

    @Test
    void updatePost_byOtherUser_shouldReturn403() throws Exception {
        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle("Title");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        User other = new User();
        other.setEmail("other@example.com");
        other.setUsername("other");
        other.setPassword(passwordEncoder.encode("123456"));
        other.setNickname("Other");
        other.setRole(Role.USER.name());
        other.setIsBanned(false);
        userMapper.insert(other);
        String otherToken = "Bearer " + jwtUtil.generateToken(other.getId(), other.getEmail(), other.getRole());

        String json = "{\"boardSlug\":\"tech\",\"title\":\"Hacked\"}";
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        mockMvc.perform(multipart("/api/posts/" + post.getId())
                        .file(dataPart)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void removePost_byAuthor_shouldMarkRemoved() throws Exception {
        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle("To Remove");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void removePost_byAdmin_shouldSucceed() throws Exception {
        Post post = new Post();
        post.setBoardId(boardId);
        post.setUserId(userId);
        post.setTitle("Admin Remove");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createPost_withInvalidImageType_shouldFail() throws Exception {
        String json = "{\"boardSlug\":\"tech\",\"title\":\"Hello\"}";
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
        MockMultipartFile badImage = new MockMultipartFile("images", "test.txt", "text/plain", "not an image".getBytes());

        mockMvc.perform(multipart("/api/posts")
                        .file(dataPart)
                        .file(badImage)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("仅支持 jpg/png/webp 格式的图片"));
    }
}

package com.example.tbmini;

import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Role;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.UserMapper;
import com.example.tbmini.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ReportIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    private String user1Token;
    private String user2Token;
    private String user3Token;
    private String adminToken;
    private Long postId;

    @BeforeEach
    void setUp() {
        User user1 = createUser("user1@example.com", "user1", "User1", Role.USER);
        User user2 = createUser("user2@example.com", "user2", "User2", Role.USER);
        User user3 = createUser("user3@example.com", "user3", "User3", Role.USER);
        User admin = createUser("admin@example.com", "admin", "Admin", Role.ADMIN);

        user1Token = "Bearer " + jwtUtil.generateToken(user1.getId(), user1.getEmail(), user1.getRole());
        user2Token = "Bearer " + jwtUtil.generateToken(user2.getId(), user2.getEmail(), user2.getRole());
        user3Token = "Bearer " + jwtUtil.generateToken(user3.getId(), user3.getEmail(), user3.getRole());
        adminToken = "Bearer " + jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole());

        Board board = new Board();
        board.setName("技术交流");
        board.setSlug("tech");
        board.setDescription("技术讨论");
        board.setIsActive(true);
        boardMapper.insert(board);

        Post post = new Post();
        post.setBoardId(board.getId());
        post.setUserId(user1.getId());
        post.setTitle("Test Post");
        post.setStatus("PUBLISHED");
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);
        postId = post.getId();
    }

    private User createUser(String email, String username, String nickname, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));
        user.setNickname(nickname);
        user.setRole(role.name());
        user.setIsBanned(false);
        userMapper.insert(user);
        return user;
    }

    @Test
    void report_shouldBeIdempotent() throws Exception {
        Map<String, Object> req = Map.of("targetType", "POST", "targetId", postId, "reasonCode", "SPAM");

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/reports")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void threeReports_shouldTriggerAutoFlag() throws Exception {
        Map<String, Object> req = Map.of("targetType", "POST", "targetId", postId, "reasonCode", "SPAM");

        mockMvc.perform(post("/api/reports").header("Authorization", user1Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reports").header("Authorization", user2Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reports").header("Authorization", user3Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/mod-queue/posts").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].id").value(postId.intValue()))
                .andExpect(jsonPath("$.data.records[0].status").value("PENDING_REVIEW"));

        mockMvc.perform(get("/api/admin/actions").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].action").value("AUTO_FLAGGED"));
    }

    @Test
    void adminApprove_shouldPublishPost() throws Exception {
        triggerAutoFlag();

        mockMvc.perform(post("/api/admin/posts/" + postId + "/approve")
                        .header("Authorization", adminToken)
                        .param("reason", "审核通过"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/admin/actions").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.action=='APPROVE')].action").exists());
    }

    @Test
    void adminRemove_shouldRemovePost() throws Exception {
        triggerAutoFlag();

        mockMvc.perform(post("/api/admin/posts/" + postId + "/remove")
                        .header("Authorization", adminToken)
                        .param("reason", "违规内容"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        mockMvc.perform(get("/api/admin/actions").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.action=='REMOVE')].action").exists());
    }

    @Test
    void adminReports_shouldListOpenReports() throws Exception {
        Map<String, Object> req = Map.of("targetType", "POST", "targetId", postId, "reasonCode", "SPAM");
        mockMvc.perform(post("/api/reports").header("Authorization", user1Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/reports").header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].targetId").value(postId.intValue()))
                .andExpect(jsonPath("$.data[0].status").value("OPEN"));
    }

    private void triggerAutoFlag() throws Exception {
        Map<String, Object> req = Map.of("targetType", "POST", "targetId", postId, "reasonCode", "SPAM");
        mockMvc.perform(post("/api/reports").header("Authorization", user1Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reports").header("Authorization", user2Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reports").header("Authorization", user3Token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}

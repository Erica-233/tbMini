package com.example.tbmini;

import com.example.tbmini.domain.entity.Board;
import com.example.tbmini.domain.entity.Comment;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Role;
import com.example.tbmini.domain.entity.User;
import com.example.tbmini.mapper.BoardMapper;
import com.example.tbmini.mapper.CommentMapper;
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
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private Long userId;
    private Long adminId;
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
    void createRootComment_shouldSucceed() throws Exception {
        Map<String, Object> req = Map.of("postId", postId, "bodyMd", "Root comment");

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bodyMd").value("Root comment"))
                .andExpect(jsonPath("$.data.parentId").doesNotExist());
    }

    @Test
    void createReply_shouldSucceed() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("Root");
        root.setBodyMd("Root");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        Map<String, Object> req = Map.of("postId", postId, "parentId", root.getId(), "bodyMd", "Reply");

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.bodyMd").value("Reply"))
                .andExpect(jsonPath("$.data.parentId").value(root.getId().intValue()));
    }

    @Test
    void createReplyToReply_shouldFail() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("Root");
        root.setBodyMd("Root");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        Comment reply = new Comment();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setParentId(root.getId());
        reply.setContent("Reply");
        reply.setBodyMd("Reply");
        reply.setStatus("PUBLISHED");
        commentMapper.insert(reply);

        Map<String, Object> req = Map.of("postId", postId, "parentId", reply.getId(), "bodyMd", "Nested");

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("只能回复根评论"));
    }

    @Test
    void listComments_shouldReturnRootsWithChildren() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("Root");
        root.setBodyMd("Root");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        Comment child = new Comment();
        child.setPostId(postId);
        child.setUserId(userId);
        child.setParentId(root.getId());
        child.setContent("Child");
        child.setBodyMd("Child");
        child.setStatus("PUBLISHED");
        commentMapper.insert(child);

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].bodyMd").value("Root"))
                .andExpect(jsonPath("$.data[0].children.length()").value(1))
                .andExpect(jsonPath("$.data[0].children[0].bodyMd").value("Child"));
    }

    @Test
    void removeComment_byAuthor_shouldSoftDelete() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("To delete");
        root.setBodyMd("To delete");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        mockMvc.perform(delete("/api/comments/" + root.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void removeComment_byAdmin_shouldSucceed() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("Admin delete");
        root.setBodyMd("Admin delete");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        mockMvc.perform(delete("/api/comments/" + root.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void removeComment_byOtherUser_shouldReturn403() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setContent("Protected");
        root.setBodyMd("Protected");
        root.setStatus("PUBLISHED");
        commentMapper.insert(root);

        User other = new User();
        other.setEmail("other@example.com");
        other.setUsername("other");
        other.setPassword(passwordEncoder.encode("123456"));
        other.setNickname("Other");
        other.setRole(Role.USER.name());
        other.setIsBanned(false);
        userMapper.insert(other);
        String otherToken = "Bearer " + jwtUtil.generateToken(other.getId(), other.getEmail(), other.getRole());

        mockMvc.perform(delete("/api/comments/" + root.getId())
                        .header("Authorization", otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void removedComment_shouldBeHiddenForGuests() throws Exception {
        Comment root = new Comment();
        root.setPostId(postId);
        root.setUserId(userId);
        root.setStatus("REMOVED");
        commentMapper.insert(root);

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

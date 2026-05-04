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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class BoardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userId;

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

        Board active = new Board();
        active.setName("技术交流");
        active.setSlug("tech");
        active.setDescription("技术讨论板块");
        active.setIsActive(true);
        boardMapper.insert(active);

        Board inactive = new Board();
        inactive.setName("隐藏板块");
        inactive.setSlug("hidden");
        inactive.setDescription("已下线");
        inactive.setIsActive(false);
        boardMapper.insert(inactive);

        Post p1 = new Post();
        p1.setBoardId(active.getId());
        p1.setUserId(userId);
        p1.setTitle("Hello World");
        p1.setContent("<p>First post content</p>");
        p1.setStatus("PUBLISHED");
        p1.setLikeCount(5);
        p1.setCommentCount(0);
        postMapper.insert(p1);

        Post p2 = new Post();
        p2.setBoardId(active.getId());
        p2.setUserId(userId);
        p2.setTitle("Draft Post");
        p2.setContent("Not published");
        p2.setStatus("DRAFT");
        p2.setLikeCount(0);
        p2.setCommentCount(0);
        postMapper.insert(p2);
    }

    @Test
    void list_shouldReturnOnlyActiveBoards() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].slug").value("tech"));
    }

    @Test
    void get_shouldReturnActiveBoard() throws Exception {
        mockMvc.perform(get("/api/boards/tech"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("技术交流"))
                .andExpect(jsonPath("$.data.slug").value("tech"));
    }

    @Test
    void get_inactiveBoard_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/boards/hidden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void posts_shouldReturnOnlyPublished() throws Exception {
        mockMvc.perform(get("/api/boards/tech/posts?page=1&pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Hello World"))
                .andExpect(jsonPath("$.data.records[0].summary").value("First post content"));
    }

    @Test
    void posts_nonexistentBoard_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/boards/unknown/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}

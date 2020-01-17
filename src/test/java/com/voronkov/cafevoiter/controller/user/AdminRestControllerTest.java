package com.voronkov.cafevoiter.controller.user;

import com.voronkov.cafevoiter.model.Cafe;
import com.voronkov.cafevoiter.model.Role;
import com.voronkov.cafevoiter.model.User;
import com.voronkov.cafevoiter.service.CafeService;
import com.voronkov.cafevoiter.service.UserService;
import com.voronkov.cafevoiter.utils.json.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.voronkov.cafevoiter.CafeTestData.assertMatch;
import static com.voronkov.cafevoiter.CafeTestData.*;
import static com.voronkov.cafevoiter.TestUtil.readFromJson;
import static com.voronkov.cafevoiter.UserTestData.assertMatch;
import static com.voronkov.cafevoiter.UserTestData.contentJson;
import static com.voronkov.cafevoiter.UserTestData.*;
import static com.voronkov.cafevoiter.utils.json.JsonUtil.writeValue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private CafeService cafeService;

    private static final String REST_USER_URL = "/admin/users/";
    private static final String REST_CAFE_URL = "/admin/cafes/";

    //For restaurant

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCafe() throws Exception {
        Cafe created = getCreated();

        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.post(REST_CAFE_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(writeValue(created)));

        Cafe returned = readFromJson(actions, Cafe.class);
        created.setId(returned.getId());

        assertMatch(returned, created);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateCafe() throws Exception {
        Cafe updated = getUpdated();

        mockMvc.perform(MockMvcRequestBuilders.put(REST_CAFE_URL + CAFE1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updated)))
                .andExpect(status().isNoContent());

        assertMatch(cafeService.getById(CAFE1_ID), updated);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCafe() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(REST_CAFE_URL + CAFE1_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createInvalid() throws Exception {
        Cafe invalid = new Cafe(null, null, LocalDate.now(), null);
        mockMvc.perform(MockMvcRequestBuilders.post(REST_CAFE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(invalid)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateInvalid() throws Exception {
        Cafe invalid = new Cafe(CAFE1_ID, null, null, null);
        mockMvc.perform(MockMvcRequestBuilders.put( REST_CAFE_URL + CAFE1_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.writeValue(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    //For users

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REST_USER_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(contentJson(userService.getAll()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getOneUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REST_USER_URL + ADMIN_ID))
                .andExpect(status().isOk())
                .andDo(print())
                // https://jira.spring.io/browse/SPR-14472
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(contentJson(userService.findById(ADMIN_ID)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser() throws Exception {
        User expected = new User(null, "new@gmail.com", "newPass",   Role.ROLE_USER);
        ResultActions action = mockMvc.perform(MockMvcRequestBuilders.post(REST_USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(expected, "newPass")))
                .andExpect(status().isCreated());
        User returned = readFromJson(action, User.class);

        expected.setId(returned.getId());
        assertMatch(returned, expected);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(REST_USER_URL + USER_ID))
                .andDo(print())
                .andExpect(status().isNoContent());
        assertMatch(userService.getAll(), List.of(ADMIN, USER2));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNotFoundUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(REST_USER_URL + 1))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser() throws Exception {
        User updated = new User(USER);
        updated.setEmail("UpdatedEmail");
        updated.setRoles(Collections.singletonList(Role.ROLE_ADMIN));

        mockMvc.perform(MockMvcRequestBuilders.put(REST_USER_URL + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithPassword(updated, USER.getPassword())))
                .andExpect(status().isNoContent());

        assertMatch(userService.findById(USER_ID), updated);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getByEmail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(REST_USER_URL + "by?email=" + ADMIN.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(contentJson(ADMIN));
    }
}
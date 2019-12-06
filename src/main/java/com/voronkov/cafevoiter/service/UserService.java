package com.voronkov.cafevoiter.service;

import com.voronkov.cafevoiter.model.Role;
import com.voronkov.cafevoiter.model.User;
import com.voronkov.cafevoiter.repository.CrudUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static Logger log = LoggerFactory.getLogger(UserService.class);

    private final CrudUserRepository userRepository;

    @Autowired
    public UserService(CrudUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAll() {
        List<User> result = userRepository.findAll();
        log.info("IN найдено {} юзеров", result.size());
        return result;

    }

    public User findById(int id) {
        User result = userRepository.findById(id).orElse(null);
        if (result == null) {
            log.warn("IN юзер с id: {} не найден", id);
            return null;
        }
        log.info("IN юзер с id: {} найден", id);
        return result;
    }

    public User save(User user) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);
        log.info("IN Юзер {} добавлен", user.toString());
        return userRepository.save(user);
    }

    public User update(User user) {
        log.info("IN юзер {} обновлён", user.getName());
        return userRepository.save(user);
    }

    public void delete(User user) {
        log.info("IN юзер {} удалён", user.getName());
        userRepository.delete(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
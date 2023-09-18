package com.ysoft.geecon.repo;

import com.ysoft.geecon.dto.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class UsersRepo {
    private final Map<String, User> users = new HashMap<>();

    public UsersRepo() {
        register(new User("bob", "Password1"));
    }

    public Optional<User> getUser(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public void register(User user) {
        users.put(user.login(), user);
    }
}

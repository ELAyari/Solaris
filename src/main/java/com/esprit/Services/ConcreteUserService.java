package com.esprit.Services;

import com.esprit.models.User;

import java.sql.SQLException;

/**
 * Concrete implementation of UserService to avoid anonymous inner class issues
 */
public class ConcreteUserService extends UserService {

    public ConcreteUserService() throws SQLException {
        super();
    }

    @Override
    public void add(User user) {
        // Implementation not needed for login
    }

    @Override
    public void delete(int id) {

    }
}

package org.springframework.labs.eprescribing.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.labs.eprescribing.model.User;

public interface UserRepository {

    void save(User user) throws DataAccessException;
}

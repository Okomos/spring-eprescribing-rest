package org.springframework.labs.eprescribing.repository.springdatajpa;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.Repository;
import org.springframework.labs.eprescribing.model.User;
import org.springframework.labs.eprescribing.repository.UserRepository;

@Profile("spring-data-jpa")
public interface SpringDataUserRepository extends UserRepository, Repository<User, String>  {

}

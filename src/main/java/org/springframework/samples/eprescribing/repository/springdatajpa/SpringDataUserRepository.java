package org.springframework.samples.eprescribing.repository.springdatajpa;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.Repository;
import org.springframework.samples.eprescribing.model.User;
import org.springframework.samples.eprescribing.repository.UserRepository;

@Profile("spring-data-jpa")
public interface SpringDataUserRepository extends UserRepository, Repository<User, String>  {

}

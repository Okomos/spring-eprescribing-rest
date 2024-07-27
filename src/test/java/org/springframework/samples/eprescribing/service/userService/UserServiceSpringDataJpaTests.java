package org.springframework.samples.eprescribing.service.userService;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"spring-data-jpa", "hsqldb"})
class UserServiceSpringDataJpaTests extends AbstractUserServiceTests {

}

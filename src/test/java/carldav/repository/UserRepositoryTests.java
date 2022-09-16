package carldav.repository;

import carldav.CarldavApplication;
import carldav.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CarldavApplication.class})
@Transactional
@Rollback
class UserRepositoryTests {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JdbcAggregateOperations template;

  @Test
  void testFindByEmailIgnoreCase() {
    var user = new User();
    user.setEmail("user1@localhost.com");
    user.setPassword("user1password");

    template.save(user);

    assertThat(userRepository.findByEmailIgnoreCase("user1@localhost.com")).isNotNull();
    assertThat(userRepository.findByEmailIgnoreCase("USER1@LOCALHOST.COM")).isNotNull();
  }
}

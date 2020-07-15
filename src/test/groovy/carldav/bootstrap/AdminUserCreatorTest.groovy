package carldav.bootstrap

import carldav.entity.User
import carldav.repository.UserRepository
import org.junit.Test
import org.springframework.security.crypto.password.PasswordEncoder

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

/**
 * @author Kamill Sokol
 */
class AdminUserCreatorTest {

    public static final String IGNORE = "ignore"

    private UserRepository userRepository = mock(UserRepository.class)
    private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class)
    private User user = mock(User.class)

    private AdminUserCreator uut = new AdminUserCreator(userRepository, passwordEncoder, IGNORE, IGNORE)

    @Test
    void adminPasswordEqual() {
        when(userRepository.findByEmailIgnoreCase(IGNORE)).thenReturn(user)
        when(user.getPassword()).thenReturn(IGNORE)

        uut.onApplicationEvent(null)

        verify(user, never()).setPassword(anyString())
        verify(userRepository, never()).save(user)
    }

    @Test
    void adminPasswordNotEqual() {
        when(userRepository.findByEmailIgnoreCase(IGNORE)).thenReturn(user)
        when(user.getPassword()).thenReturn("different")
        when(passwordEncoder.encode(IGNORE)).thenReturn("encoded")

        uut.onApplicationEvent(null)

        verify(user).setPassword("encoded")
        verify(userRepository).save(user)
    }
}

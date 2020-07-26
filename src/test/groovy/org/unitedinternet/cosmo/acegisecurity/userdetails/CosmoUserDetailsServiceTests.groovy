package org.unitedinternet.cosmo.acegisecurity.userdetails

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import carldav.repository.UserRepository
import carldav.entity.User

import static org.hamcrest.Matchers.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CosmoUserDetailsServiceTests {

    private static String EMAIL = "email"

    private UserDetailsService uut
    private User user

    @BeforeEach
    void before() {
        UserRepository userRepository = mock(UserRepository.class)
        uut = new CosmoUserDetailsService(userRepository)

        user = mock(User.class)

        when(user.getEmail()).thenReturn(EMAIL)
        when(user.getPassword()).thenReturn(EMAIL)
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(user)
    }

    @Test
    void hasRootRole() {
        when(user.getRole()).thenReturn("ROLE_ADMIN")

        UserDetails userDetails = uut.loadUserByUsername(EMAIL)

        assertThat(userDetails.getAuthorities(), everyItem(hasProperty("authority", is("ROLE_ADMIN"))))
    }

    @Test
    void isLockedAccount() {
        when(user.isLocked()).thenReturn(true)

        UserDetails userDetails = uut.loadUserByUsername(EMAIL)

        assertThat(userDetails.isAccountNonLocked(), is(false))
    }
}

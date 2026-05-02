package otvosuzlet.javitasnyilntarto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.dto.LoginResponse;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.TokensResponseDTO;
import otvosuzlet.javitasnyilntarto.dto.UserSearchRequest;
import otvosuzlet.javitasnyilntarto.model.UserDto;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;
import otvosuzlet.javitasnyilntarto.exceptions.InvalidCredentialsException;
import otvosuzlet.javitasnyilntarto.exceptions.UserNotFoundException;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.repository.RefreshTokenRepository;
import otvosuzlet.javitasnyilntarto.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private ConversionService conversionService;

    @Test
    void findUserByIdShouldThrowWhenMissing() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(999));
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchUsersShouldReturnPagedResponse() {
        User user = new User();
        user.setUsername("testuser");
        Page<User> userPage = new PageImpl<>(java.util.List.of(user), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest search = new UserSearchRequest();

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(userPage);
        UserDto userDto = new UserDto("testuser", "Test User", 1, Set.of());
        when(conversionService.convert(any(User.class), eq(UserDto.class))).thenReturn(userDto);
        when(conversionService.convert(any(Page.class), eq(PageResponse.class))).thenReturn(new PageResponse<>());

        PageResponse<UserDto> response = userService.searchUsers(search, pageable);

        assertNotNull(response);
    }

    @Test
    void verifyLoginShouldReturnLoginResponseWhenCredentialsValid() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("john");
        request.setPassword("password");

        User user = new User();
        user.setId(1);
        user.setUsername("john");
        user.setName("John Doe");
        user.setAuthorities(Set.of(UserAuthorities.MODIFY_JOBS));

        when(authManager.authenticate(any())).thenReturn(
            new UsernamePasswordAuthenticationToken("john", "password", null));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        TokensResponseDTO tokens = new TokensResponseDTO("access", "refresh");
        when(jwtService.generateTokens("john")).thenReturn(tokens);
        when(jwtService.extractExpiration("refresh")).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(userRepository.save(any())).thenReturn(user);

        LoginResponse response = userService.verify(request, "192.168.1.1", "Mozilla");

        assertEquals("John Doe", response.getName());
        assertEquals(tokens, response.getTokens());
    }

    @Test
    void verifyLoginShouldThrowInvalidCredentialsOnBadCredentials() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("john");
        request.setPassword("wrong");

        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, 
            () -> userService.verify(request, "192.168.1.1", "Mozilla"));
    }

    @Test
    void deleteUserShouldCallRepositoryDelete() {
        User user = new User();
        user.setId(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        userService.deleteUser(5);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldThrowNotFoundWhenUserMissing() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999));
    }

    @Test
    void resetPasswordShouldGenerateNewPasswordAndUpdate() {
        User user = new User();
        user.setId(1);
        user.setUsername("john");
        Optional<User> optionalUser = Optional.of(user);
        when(userRepository.findById(1)).thenReturn(optionalUser);
        User savedUser = Mockito.mock(User.class);
        when(userRepository.save(user)).thenReturn(savedUser);

        var response = userService.resetPassword(1);

        assertNotNull(response);
        verify(userRepository).save(user);
    }
}

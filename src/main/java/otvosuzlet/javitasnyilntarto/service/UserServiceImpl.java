package otvosuzlet.javitasnyilntarto.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.dto.LoginResponse;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.RegisterUserRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetOwnPasswordRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetPasswordResponse;
import otvosuzlet.javitasnyilntarto.dto.TokensResponseDTO;
import otvosuzlet.javitasnyilntarto.dto.UserSearchRequest;
import otvosuzlet.javitasnyilntarto.enums.UserAuthorities;
import otvosuzlet.javitasnyilntarto.exceptions.InvalidCredentialsException;
import otvosuzlet.javitasnyilntarto.exceptions.UserNotFoundException;
import otvosuzlet.javitasnyilntarto.exceptions.UseranemeNotUniqueException;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.RefreshToken;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.model.UserDto;
import otvosuzlet.javitasnyilntarto.repository.RefreshTokenRepository;
import otvosuzlet.javitasnyilntarto.repository.UserRepository;
import otvosuzlet.javitasnyilntarto.specification.UserSearchSpec;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService{
    
    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ConversionService conversionService;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    AuthenticationManager authManager;
    
    private static final int PASS_LENGTH = 20;


    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResetPasswordResponse registerUser(RegisterUserRequest request){
        try{
            User user = assingValuesToUser(request, new User());
            String generatedPassword = generateStrongPassword(PASS_LENGTH);
            user.setPassword(bCryptPasswordEncoder.encode(generatedPassword));
            userRepository.save(user);

            return new ResetPasswordResponse(generatedPassword);
        
        }catch(UseranemeNotUniqueException e){
            throw new ValidationException("username", "validation.username.already.exists");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void updateUser(Integer id, RegisterUserRequest request) {
        User user = findUserById(id);
        try{
            userRepository.save(assingValuesToUser(request, user));
        }catch(UseranemeNotUniqueException e){
            throw new ValidationException("username", "validation.username.already.exists");
        }
    }

    private User assingValuesToUser(RegisterUserRequest request, User user) {
        boolean usernameChanged = user.getId() == null || !request.getUsername().equals(user.getUsername());
        if (usernameChanged && userRepository.existsByUsername(request.getUsername())) {
            throw new UseranemeNotUniqueException("A felhasználónév már foglalt");
        }

        if(request.getName() == null || request.getName().isBlank()){
            user.setName(null);
        }
        else{
            user.setName(request.getName());
        }
        
        user.setUsername(request.getUsername());
        user.setAuthorities(request.getAuthorities());
        return user;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public User findUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(()->new UserNotFoundException("Nincs ilyen felhasználó"));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> searchUsers(UserSearchRequest search, Pageable pageable) {
        Page<UserDto> page = userRepository.findAll(UserSearchSpec.withFilters(search), pageable)
            .map(user -> conversionService.convert(user, UserDto.class));

        @SuppressWarnings("unchecked")
        PageResponse<UserDto> response = conversionService.convert(page, PageResponse.class);
        return response;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void createAdminIfNotExists() {
        if (userRepository.count() > 0) {
            return;
        }
        
        User root = new User();
        root.setAuthorities(Set.copyOf(Arrays.asList(UserAuthorities.values())));
        String rootPass = generateStrongPassword(PASS_LENGTH);
        System.out.println("----------------------");
        System.out.println("\tRoot pass: "+rootPass);
        System.out.println("----------------------");

        root.setPassword(bCryptPasswordEncoder.encode(rootPass));
        root.setUsername("root");
        userRepository.save(root);
    }
    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Nem található felhasználó a törléshez: " + id));
        userRepository.delete(userToDelete);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Nem található felhasználó: " + id));

        String newPassword = generateStrongPassword(PASS_LENGTH);
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);


        return new ResetPasswordResponse(newPassword);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetOwnPassword(String username, ResetOwnPasswordRequest request) {
        User user = findByUsername(username);

        invalidateOtherRefreshTokensByUser(request.getRefreshToken(), user);

        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String generateStrongPassword(int length) {
        // Ensure a mix of categories and avoid ambiguous chars if you prefer.
        final String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        final String lower = "abcdefghijkmnpqrstuvwxyz";
        final String digits = "23456789";
        final String all = upper + lower + digits;

        SecureRandom rng = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        sb.append(upper.charAt(rng.nextInt(upper.length())));
        sb.append(lower.charAt(rng.nextInt(lower.length())));
        sb.append(digits.charAt(rng.nextInt(digits.length())));

        for (int i = sb.length(); i < length; i++) {
            sb.append(all.charAt(rng.nextInt(all.length())));
        }

        // Shuffle
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Nincs ilyen felhasználó"));
    }


    /** {@inheritDoc} */
    @Override
    @Transactional
    public LoginResponse verify(LoginRequestDto user, String ipAddress, String userAgent) {
        try {
            Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            if (!authentication.isAuthenticated()) {
                throw new InvalidCredentialsException("Hibás felhasználónév vagy jelszó");
            }

            User persistedUser = findByUsername(user.getUsername());
            TokensResponseDTO tokens = jwtService.generateTokens(user.getUsername());
            saveRefreshToken(tokens.getRefreshToken(), persistedUser, ipAddress, userAgent);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAuthorities(persistedUser.getAuthorities());
            loginResponse.setTokens(tokens);
            loginResponse.setName(persistedUser.getName());
            return loginResponse;
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Hibás felhasználónév vagy jelszó");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LoginResponse refresh(String refreshToken, String ipAddress, String userAgent) {
        String tokenType = jwtService.extractTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {
            throw new InvalidCredentialsException("Invalid token type");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token expired");
        }
        RefreshToken previousToken = findTokenByRefreshToken(refreshToken);

        if (!isRefreshTokenRevoked(previousToken)) {
            throw new InvalidCredentialsException("Refresh token has been revoked");
        }
        invalidateRefreshToken(previousToken);

        String username = jwtService.extractUserName(refreshToken);

        User persistedUser = findByUsername(username);
        TokensResponseDTO tokens = jwtService.generateTokens(username);
        saveRefreshToken(tokens.getRefreshToken(), persistedUser, ipAddress, userAgent);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAuthorities(persistedUser.getAuthorities());
        loginResponse.setTokens(tokens);
        loginResponse.setName(persistedUser.getName());
        return loginResponse;

    }
    private String sha256Base64Url(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
    private RefreshToken saveRefreshToken(String token, User user, String ipAddress, String userAgent) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(sha256Base64Url(token));
        refreshToken.setUser(user);
        refreshToken.setRevokedAt(null);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        Instant expiresAt = jwtService.extractExpiration(token).toInstant();
        refreshToken.setExpiresAt(expiresAt);

        if (user.getRefreshTokens() == null) {
            user.setRefreshTokens(new HashSet<>());
        }
        user.getRefreshTokens().add(refreshToken);

        userRepository.save(user);
        return refreshToken;
    }
    
    public RefreshToken invalidateRefreshToken(RefreshToken token){
        if(token.getRevokedAt() == null || token.getRevokedAt().isAfter(Instant.now())){
            token.setRevokedAt(Instant.now());
        }
        refreshTokenRepository.save(token);
        return token;
    }
    /** {@inheritDoc} */
    @Override
    @Transactional
    public RefreshToken invalidateRefreshTokenString(String token){
        RefreshToken refreshToken = findTokenByRefreshToken(token);
        return invalidateRefreshToken(refreshToken);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void invalidateOtherRefreshTokensByUsername(String token, String authenticatedUsername) {
        User user = findByUsername(authenticatedUsername);
        invalidateOtherRefreshTokensByUser(token, user);
    }

    @Transactional
    private void invalidateOtherRefreshTokensByUser(String token, User user) {
        String tokenHash = sha256Base64Url(token);

        user.getRefreshTokens().stream()
            .filter(t -> !t.getTokenHash().equals(tokenHash))
            .forEach(this::invalidateRefreshToken);
    }

    private boolean isRefreshTokenRevoked(RefreshToken token){
        return token.getRevokedAt() == null;
    }
    private RefreshToken findTokenByRefreshToken(String token){
        String tokenHash = sha256Base64Url(token);
        return refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new InvalidCredentialsException("Refresh token not found"));
    }
}

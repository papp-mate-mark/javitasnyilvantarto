package otvosuzlet.javitasnyilntarto.service;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.dto.LoginResponse;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.RegisterUserRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetOwnPasswordRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetPasswordResponse;
import otvosuzlet.javitasnyilntarto.dto.UserSearchRequest;
import otvosuzlet.javitasnyilntarto.exceptions.InvalidCredentialsException;
import otvosuzlet.javitasnyilntarto.model.RefreshToken;
import otvosuzlet.javitasnyilntarto.model.User;
import otvosuzlet.javitasnyilntarto.model.UserDto;

import org.springframework.data.domain.Pageable;


public interface UserService {
    /**
     * Registers a new user based on a registration request, with username conflict validation.
     * 
     * @param request The data transfer object containing new user details.
     * @return A response containing reset password details if needed, or registration outcome.
     */
    ResetPasswordResponse registerUser(RegisterUserRequest request);

    /**
     * Updates an existing user with new details, with username conflict validation.
     * 
     * @param id The ID of the user to update.
     * @param request The data transfer object containing the updated user details.
     */
    void updateUser(Integer id, RegisterUserRequest request);

    /**
     * Retrieves a user by their ID.
     * 
     * @param id The ID of the user to retrieve.
     * @return The found user.
     */
    User findUserById(Integer id);

    /**
     * Searches for users based on given criteria and pagination.
     * 
     * @param search The search request containing filters.
     * @param pageable The pagination information.
     * @return A paginated response of user DTOs matching the search criteria.
     */
    PageResponse<UserDto> searchUsers(UserSearchRequest search, Pageable pageable);

    /**
     * Creates an initial admin user if no admin exists in the system.
     */
    void createAdminIfNotExists();

    /**
     * Deletes a user by their ID.
     * 
     * @param id The ID of the user to delete.
     */
    void deleteUser(Integer id);

    /**
     * Resets the password for the specified user ID.
     * 
     * @param id The ID of the user whose password should be reset.
     * @return A response containing the new password data.
     */
    ResetPasswordResponse resetPassword(Integer id);

    /**
     * Updates the authenticated user's own password.
     *
     * @param username The authenticated username.
     * @param request The requested new password.
     */
    void resetOwnPassword(String username, ResetOwnPasswordRequest request);

    /**
     * Finds a user by their username.
     * 
     * @param username The username of the user to find.
     * @return The found user.
     */
    User findByUsername(String username); 

    /**
     * Verifies user credentials for login.
     *
     * @param user The login request containing username and password.
     * @param ipAddress The client IP address.
     * @param userAgent The client user agent.
     * @return A login response containing the auth tokens if verification is successful.
     */
    LoginResponse verify(LoginRequestDto user, String ipAddress, String userAgent);

    /**
     * Generates a new auth token based on a valid refresh token.
     *
     * @param refreshtoken The refresh token to use for generating new tokens.
     * @param ipAddress The client IP address.
     * @param userAgent The client user agent.
     * @return A login response containing the new auth tokens.
     */
    LoginResponse refresh(String refreshtoken, String ipAddress, String userAgent);

    /**
     * Invalidates the given refresh token.
     * Basically logs the user out.
     *
     * @param token The refresh token string to invalidate.
     * @return The invalidated refresh token entity.
     */
    RefreshToken invalidateRefreshTokenString(String token);

    /**
     * Invalidates all refresh tokens for a user except the provided one.
     * Used for "logout from all other sessions" functionality.
     * Only the authenticated user can invalidate their own other tokens.
     *
     * @param token The refresh token to keep valid.
     * @param authenticatedUsername The username of the authenticated user performing the action.
     * @throws InvalidCredentialsException if token doesn't belong to the authenticated user.
     */
    void invalidateOtherRefreshTokensByUsername(String token, String authenticatedUsername);

    
}

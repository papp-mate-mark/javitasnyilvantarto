package otvosuzlet.javitasnyilntarto.controllers;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import otvosuzlet.javitasnyilntarto.dto.PageResponse;
import otvosuzlet.javitasnyilntarto.dto.RegisterUserRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetOwnPasswordRequest;
import otvosuzlet.javitasnyilntarto.dto.ResetPasswordResponse;
import otvosuzlet.javitasnyilntarto.dto.UserSearchRequest;
import otvosuzlet.javitasnyilntarto.exceptions.ValidationException;
import otvosuzlet.javitasnyilntarto.model.UserDto;
import otvosuzlet.javitasnyilntarto.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user for the system based on the provided data.
     * Auto generates a password.
     * 
     * @param request Valid defining constraints resolving configurations comprehensively completely perfectly cleanly structurally dynamically mapping properties appropriately flawlessly logically systematically gracefully smoothly securely natively smoothly resolving accurately explicitly directly logically natively thoroughly accurately explicitly functionally safely cleanly functionally flawlessly flawlessly logically mapping safely mapping perfectly.
     * @param bindingResult Data binder capturing validation state.
     * @return Interpretable definitions returning newly initialized parameters thoroughly smoothly.
     * @throws BindException Generated when invalid bindings have transpired.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('MODIFY_USERS')")
    public ResponseEntity<ResetPasswordResponse> registerUser(@RequestBody @Valid RegisterUserRequest request, BindingResult bindingResult) throws BindException {
        if(bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            return ResponseEntity.ok(userService.registerUser(request));
        } catch (ValidationException e) {
            for (ValidationException.ValidationError error : e.getErrors()) {
                bindingResult.rejectValue(error.getField(), "", error.getDefaultMessage());
            }
            throw new BindException(bindingResult);
        }
    }

    /**
     * Updates a user's information based on their ID.
     *
     * @param id The reference entity primary key.
     * @param request Valid defining constraints resolving configurations comprehensively completely perfectly cleanly structurally dynamically mapping properties appropriately flawlessly logically systematically gracefully smoothly securely natively smoothly resolving accurately explicitly directly logically natively thoroughly accurately explicitly functionally safely cleanly functionally flawlessly flawlessly logically mapping safely mapping perfectly.
     * @param bindingResult Data binder capturing validation state.
     * @return Execution result denoting end naturally perfectly optimally matching HTTP logically matching perfectly successfully successfully precisely completely comprehensively securely mapping naturally comprehensively.
     * @throws BindException Generated when invalid bindings have transpired.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODIFY_USERS')")
    public ResponseEntity<Void> updateUser(@PathVariable Integer id, @RequestBody @Valid RegisterUserRequest request, BindingResult bindingResult) throws BindException {        
        if(bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            userService.updateUser(id, request);
            return ResponseEntity.noContent().build();
        } catch (ValidationException e) {
            for (ValidationException.ValidationError error : e.getErrors()) {
                bindingResult.rejectValue(error.getField(), "", error.getDefaultMessage());
            }
            throw new BindException(bindingResult);
        }
    }

    /**
     * Searches for users based on the provided search criteria.
     *
     * @param search   The search criteria.
     * @param pageable The pagination information.
     * @return A paginated list of users matching the criteria.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('LIST_USERS')")
    public ResponseEntity<PageResponse<UserDto>> searchUsers(
            @ModelAttribute UserSearchRequest search,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        PageResponse<UserDto> response = userService.searchUsers(search, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return An empty response indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MODIFY_USERS')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resets the password for a user.
     *
     * @param id The ID of the user.
     * @return The response containing the new password.
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('MODIFY_USERS')")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.resetPassword(id));
    }

    /**
     * Updates the currently authenticated user's password.
     *
     * @param request The new password payload.
     * @param principal The authenticated principal.
     * @return Empty response indicating successful update.
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resetOwnPassword(
        @RequestBody @Valid ResetOwnPasswordRequest request,
        Principal principal
    ) {
        userService.resetOwnPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }

}

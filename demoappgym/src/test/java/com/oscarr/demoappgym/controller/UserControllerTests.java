package com.oscarr.demoappgym.controller;

import com.oscarr.demoappgym.model.dto.UserRequest;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.enums.DocumentType;
import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTests {

    private final UserService userService = mock(UserService.class);
    private final UserController userController = new UserController(userService);

    @Test
    void adminCanCreateAdminUser() {
        UserRequest request = requestWithRole(Role.ADMIN);
        var adminAuthentication = new UsernamePasswordAuthenticationToken(
                "admin@gym.local",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(request, adminAuthentication);

        assertEquals(Role.ADMIN.name(), response.getBody().getRole());
        verify(userService).save(any(User.class));
    }

    @Test
    void unauthenticatedRequestCannotCreateAdminUser() {
        UserRequest request = requestWithRole(Role.ADMIN);

        assertThrows(AccessDeniedException.class, () -> userController.createUser(request, null));
    }

    @Test
    void publicRegistrationDefaultsToRegularUser() {
        UserRequest request = requestWithRole(null);
        request.setDocumentType(null);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(request, null);

        assertEquals(Role.USER.name(), response.getBody().getRole());
        assertEquals(DocumentType.CEDULA_CIUDADANIA.name(), response.getBody().getDocumentType());
    }

    @Test
    void clientCanBeCreatedWithoutPassword() {
        UserRequest request = requestWithRole(Role.USER);
        request.setPassword(null);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(request, null);

        assertEquals(Role.USER.name(), response.getBody().getRole());
        verify(userService).save(any(User.class));
    }

    @Test
    void clientCanBeCreatedWithIdentityCardDocumentType() {
        UserRequest request = requestWithRole(Role.USER);
        request.setDocumentType(DocumentType.TARJETA_IDENTIDAD);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(request, null);

        assertEquals(DocumentType.TARJETA_IDENTIDAD.name(), response.getBody().getDocumentType());
    }

    @Test
    void registrationRequiresDocumentNumber() {
        UserRequest request = requestWithRole(null);
        request.setDocumentNumber(" ");

        assertThrows(BusinessException.class, () -> userController.createUser(request, null));
    }

    @Test
    void registrationSavesOptionalPhoto() {
        String photo = "data:image/jpeg;base64,/9j/2Q==";
        UserRequest request = requestWithRole(null);
        request.setPhotoData(photo);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.createUser(request, null);

        assertEquals(photo, response.getBody().getPhotoData());
    }

    @Test
    void creatingCoachUserPassesPhotoToService() {
        String photo = "data:image/jpeg;base64,/9j/2Q==";
        UserRequest request = requestWithRole(Role.COACH);
        request.setPhotoData(photo);
        var adminAuthentication = new UsernamePasswordAuthenticationToken(
                "admin@gym.local",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userController.createUser(request, adminAuthentication);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userService).save(user.capture());
        assertEquals(photo, user.getValue().getPhotoData());
    }

    @Test
    void updateCanReplacePhoto() {
        String photo = "data:image/jpeg;base64,/9j/2Q==";
        User user = new User();
        user.setCodeUser(1L);
        user.setPassword("existing-hash");
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserRequest request = requestWithRole(null);
        request.setPhotoData(photo);

        var response = userController.updateUser(1L, request);

        assertEquals(photo, response.getBody().getPhotoData());
    }

    @Test
    void updateCanRemovePhotoWhenExplicitlySetToNull() {
        User user = new User();
        user.setCodeUser(1L);
        user.setPassword("existing-hash");
        user.setPhotoData("data:image/jpeg;base64,/9j/2Q==");
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserRequest request = requestWithRole(null);
        request.setPhotoData(null);

        var response = userController.updateUser(1L, request);

        assertNull(response.getBody().getPhotoData());
    }

    @Test
    void updateKeepsPhotoWhenPhotoWasNotIncludedInRequest() {
        String photo = "data:image/jpeg;base64,/9j/2Q==";
        User user = new User();
        user.setCodeUser(1L);
        user.setPassword("existing-hash");
        user.setPhotoData(photo);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userController.updateUser(1L, requestWithRole(null));

        assertEquals(photo, response.getBody().getPhotoData());
    }

    @Test
    void requestDistinguishesOmittedPhotoFromExplicitRemoval() {
        UserRequest omitted = new UserRequest();
        UserRequest removed = new UserRequest();
        removed.setPhotoData(null);

        assertFalse(omitted.hasPhotoData());
        assertTrue(removed.hasPhotoData());
    }

    private UserRequest requestWithRole(Role role) {
        UserRequest request = new UserRequest();
        request.setName("Cuenta nueva");
        request.setAddress("Dirección");
        request.setPassword("password");
        request.setPhoneNumber(3001234567L);
        request.setDocumentNumber("123456789");
        request.setDocumentType(DocumentType.CEDULA_CIUDADANIA);
        request.setMail("nueva@gym.local");
        request.setRole(role);
        return request;
    }
}

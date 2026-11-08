package com.oscarr.demoappgym.service;

import com.oscarr.demoappgym.exception.BusinessException;
import com.oscarr.demoappgym.model.entity.User;
import com.oscarr.demoappgym.model.entity.Coach;
import com.oscarr.demoappgym.model.enums.Role;
import com.oscarr.demoappgym.repository.AffiliationRepository;
import com.oscarr.demoappgym.repository.CoachRepository;
import com.oscarr.demoappgym.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AffiliationRepository affiliationRepository = mock(AffiliationRepository.class);
    private final CoachRepository coachRepository = mock(CoachRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(
            userRepository, affiliationRepository, coachRepository, passwordEncoder);

    @Test
    void savingClientWithoutPasswordGeneratesAnEncodedCredential() {
        User user = new User();
        user.setRole(Role.USER);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.save(user);

        ArgumentCaptor<CharSequence> password = ArgumentCaptor.forClass(CharSequence.class);
        verify(passwordEncoder).encode(password.capture());
        assertFalse(password.getValue().toString().isBlank());
        assertEquals("encoded-password", saved.getPassword());
        verifyNoInteractions(coachRepository);
    }

    @Test
    void savingNonClientWithoutPasswordIsRejected() {
        User user = new User();
        user.setRole(Role.ADMIN);

        assertThrows(BusinessException.class, () -> userService.save(user));
        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void creatingCoachUserAlsoCreatesCoachCatalogEntry() {
        User user = new User();
        user.setName("Entrenador nuevo");
        user.setRole(Role.COACH);
        user.setPassword("temporary-password");
        user.setPhotoData("data:image/jpeg;base64,/9j/2Q==");
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.save(user);

        ArgumentCaptor<Coach> coach = ArgumentCaptor.forClass(Coach.class);
        verify(coachRepository).save(coach.capture());
        assertEquals(user.getName(), coach.getValue().getNameCoach());
        assertEquals(user.getPhotoData(), coach.getValue().getPhotoData());
    }
}

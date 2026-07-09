package tn.esprit.canvabna.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.repository.UserRepository;

/**
 * Spring Security {@link UserDetailsService} implementation.
 *
 * <p>Loads a user by matricule (stored as a string during authentication).
 * Constructor injection is used — no field injection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their matricule.
     *
     * @param username the matricule as a string (Spring Security convention)
     * @return populated {@link CustomUserDetails}
     * @throws UsernameNotFoundException if no user with that matricule exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        log.debug("Loading user by matricule: {}", username);

        final Integer matricule;
        try {
            matricule = Integer.parseInt(username);
        } catch (NumberFormatException e) {
            log.warn("Invalid matricule format: {}", username);
            throw new UsernameNotFoundException("User not found with matricule: " + username);
        }

        return userRepository.findByMatricule(matricule)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> {
                    log.warn("No user found with matricule: {}", matricule);
                    return new UsernameNotFoundException("User not found with matricule: " + matricule);
                });
    }
}

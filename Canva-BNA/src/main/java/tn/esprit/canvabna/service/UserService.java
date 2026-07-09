package tn.esprit.canvabna.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.canvabna.dto.UserResponse;
import tn.esprit.canvabna.entity.User;
import tn.esprit.canvabna.mapper.UserMapper;
import tn.esprit.canvabna.repository.UserRepository;

import java.util.List;

/**
 * Service for user management operations.
 * Separates user CRUD concerns from authentication concerns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns all registered users.
     *
     * @return list of user responses (password never exposed)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.debug("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Returns the user with the given matricule.
     *
     * @param matricule the matricule to look up
     * @return user response
     * @throws UsernameNotFoundException if no user with that matricule exists
     */
    @Transactional(readOnly = true)
    public UserResponse findByMatricule(final Integer matricule) {
        log.debug("Fetching user by matricule: {}", matricule);
        final User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with matricule: " + matricule));
        return userMapper.toResponse(user);
    }

    /**
     * Enables or disables the given user account.
     *
     * @param matricule the matricule of the user to update
     * @param enabled   the new enabled state
     * @return updated user response
     */
    @Transactional
    public UserResponse setEnabled(final Integer matricule, final boolean enabled) {
        log.info("Setting enabled={} for matricule: {}", enabled, matricule);
        final User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with matricule: " + matricule));
        user.setEnabled(enabled);
        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * Updates an existing user's details.
     *
     * @param matricule the matricule of the user to update
     * @param request   the update payload containing new structure and role
     * @return updated user response
     */
    @Transactional
    public UserResponse update(final Integer matricule, final tn.esprit.canvabna.dto.UpdateRequest request) {
        log.info("Updating user with matricule: {}", matricule);
        final User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with matricule: " + matricule));

        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setStructure(request.getStructure());
        user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    /**
     * Deletes a user by matricule.
     *
     * @param matricule the matricule of the user to delete
     */
    @Transactional
    public void delete(final Integer matricule) {
        log.info("Deleting user with matricule: {}", matricule);
        final User user = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with matricule: " + matricule));
        userRepository.delete(user);
    }
}

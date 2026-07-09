package tn.esprit.canvabna.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.esprit.canvabna.entity.User;

import java.util.Collection;
import java.util.List;

/**
 * Custom {@link UserDetails} implementation that wraps a {@link User} entity.
 *
 * <p>The Spring Security <em>username</em> is the string representation of
 * {@code matricule}, so that JWT subject and authentication principal are
 * both consistent with the domain model.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(final User user) {
        this.user = user;
    }

    // ---------------------------------------------------------- UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** Returns matricule as a string — used as JWT subject. */
    @Override
    public String getUsername() {
        return user.getMatricule().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}

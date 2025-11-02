package app.security;

import app.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthenticationMetadata implements UserDetails {

    private UUID userId;
    private String username;
    private String password;
    private UserRole userRole;
    private String permission;
    private boolean employed;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.name());
        SimpleGrantedAuthority permission = new SimpleGrantedAuthority(getPermission());

        return List.of(authority, permission);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.employed;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.employed;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.employed;
    }

    @Override
    public boolean isEnabled() {
        return this.employed;
    }
}

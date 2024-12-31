package comatching.comatching3.users.auth.oauth2.provider;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

public interface ProviderUser {
    String getProvider();

    String getSocialId();

    String getUsername();

    String getEmail();

    List<? extends GrantedAuthority> getAuthorities();

    Map<String, Object> getAttributes();
}
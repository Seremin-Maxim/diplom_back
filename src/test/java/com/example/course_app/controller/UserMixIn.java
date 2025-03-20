package com.example.course_app.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Класс-примесь (mixin) для Jackson, чтобы игнорировать поле authorities при сериализации/десериализации
 */
public abstract class UserMixIn {
    
    @JsonIgnore
    abstract Collection<? extends GrantedAuthority> getAuthorities();
    
    @JsonIgnore
    abstract boolean isAccountNonExpired();
    
    @JsonIgnore
    abstract boolean isAccountNonLocked();
    
    @JsonIgnore
    abstract boolean isCredentialsNonExpired();
    
    @JsonIgnore
    abstract boolean isEnabled();
}

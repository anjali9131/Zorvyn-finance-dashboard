package com.finance.dashboard.dto.request;

import com.finance.dashboard.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Email(message = "Must be a valid email address")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Role role;

    private Boolean active;
}

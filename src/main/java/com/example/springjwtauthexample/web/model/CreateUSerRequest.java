package com.example.springjwtauthexample.web.model;

import com.example.springjwtauthexample.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUSerRequest {

    private String username;

    private String email;

    private Set<RoleType> roles;

    private String password;
}

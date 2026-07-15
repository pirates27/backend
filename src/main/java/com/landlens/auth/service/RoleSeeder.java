package com.landlens.auth.service;

import com.landlens.auth.model.Role;
import com.landlens.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seedRoles() {
        List<String> roleNames = Arrays.asList("ADMIN", "GOVERNMENT_OFFICER", "PROVIDER", "BUYER");
        
        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription("System Role: " + roleName);
                roleRepository.save(role);
            }
        }
    }
}

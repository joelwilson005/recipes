package com.joel.recipes.config.security;

import com.joel.recipes.model.Role;
import com.joel.recipes.model.RoleType;
import com.joel.recipes.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Log
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;

    private boolean alreadySetup = false;

    public SetupDataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) return;
        if (this.roleRepository.findByAuthority(RoleType.USER.name()).isEmpty()) {
            Role userRole = new Role();
            userRole.setAuthority(RoleType.USER.name());
            this.roleRepository.save(userRole);
        }

        this.alreadySetup = true;
    }
}
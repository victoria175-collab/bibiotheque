package com.ibizabroker.bibliotheque.configuration;

import com.ibizabroker.bibliotheque.dao.RoleRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Role;
import com.ibizabroker.bibliotheque.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin48}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Set<Role> allRoles = new HashSet<>();
        for (String roleName : List.of("Admin", "User")) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setRoleName(roleName);
                        return roleRepository.save(r);
                    });
            allRoles.add(role);
        }

        if (usersRepository.findByUsername(adminUsername).isEmpty()) {
            Users admin = new Users();
            admin.setUsername(adminUsername);
            admin.setName("Administrateur");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(allRoles);
            usersRepository.save(admin);
        }
    }
}

package otvosuzlet.javitasnyilntarto.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import otvosuzlet.javitasnyilntarto.service.UserService;

@Component
public class AdminInitializer implements CommandLineRunner {
    @Autowired
    private UserService userService;
    

    @Override
    public void run(String... args) {
        userService.createAdminIfNotExists();
    }
}

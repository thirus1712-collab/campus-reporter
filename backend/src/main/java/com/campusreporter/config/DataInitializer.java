package com.campusreporter.config;

import com.campusreporter.entity.User;
import com.campusreporter.model.Department;
import com.campusreporter.model.UserRole;
import com.campusreporter.repository.UserRepository;
import com.campusreporter.repository.ComplaintRepository;
import com.campusreporter.entity.Complaint;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, ComplaintRepository complaintRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.complaintRepository = complaintRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Verifying initial campus portal user accounts...");

        // 1. Student Account
        seedUserIfNotExists(
            "student@smartcampus.edu",
            "Student@123",
            "Sarah Jenkins (Student)",
            UserRole.STUDENT,
            null,
            "9876543210"
        );

        // 2. Electrical Department
        seedUserIfNotExists(
            "electrical@smartcampus.edu",
            "Staff@123",
            "David Miller (Electrical Lead)",
            UserRole.ELECTRICAL,
            Department.ELECTRICAL,
            "9876543211"
        );

        // 3. Plumbing Department
        seedUserIfNotExists(
            "plumbing@smartcampus.edu",
            "Staff@123",
            "Robert Chen (Plumbing Supervisor)",
            UserRole.PLUMBING,
            Department.PLUMBING,
            "9876543212"
        );

        // 4. Cleaning Department
        seedUserIfNotExists(
            "cleaning@smartcampus.edu",
            "Staff@123",
            "Maria Santos (Cleaning Head)",
            UserRole.CLEANING,
            Department.CLEANING,
            "9876543213"
        );

        // 5. IT & Technical Support
        seedUserIfNotExists(
            "itsupport@smartcampus.edu",
            "Staff@123",
            "Alex Kumar (IT Specialist)",
            UserRole.IT_SUPPORT,
            Department.IT_SUPPORT,
            "9876543214"
        );

        // 6. Administrator Account
        String adminPassword = System.getenv("ADMIN_INITIAL_PASSWORD");
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            adminPassword = "Admin@123";
        }
        seedUserIfNotExists(
            "admin@smartcampus.edu",
            adminPassword,
            "Dr. Arthur Vance (Dean of Infrastructure)",
            UserRole.ADMIN,
            null,
            "9876543215"
        );

        logger.info("Campus user accounts verified successfully.");

        // Backfill workDeadline for any existing accepted complaints that do not have one set
        try {
            List<Complaint> missingDeadlines = complaintRepository.findByAcceptedAtNotNullAndWorkDeadlineIsNull();
            if (missingDeadlines != null && !missingDeadlines.isEmpty()) {
                logger.info("Backfilling 7-day work deadlines for {} previously accepted complaints...", missingDeadlines.size());
                for (Complaint c : missingDeadlines) {
                    if (c.getAcceptedAt() != null) {
                        c.setWorkDeadline(c.getAcceptedAt().plusDays(7));
                        complaintRepository.save(c);
                    }
                }
                logger.info("Successfully backfilled work deadlines.");
            }
        } catch (Exception e) {
            logger.warn("Could not backfill work deadlines on startup: {}", e.getMessage());
        }
    }

    private void seedUserIfNotExists(String email, String rawPassword, String fullName, UserRole role, Department dept, String phone) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            User user = new User(
                email.toLowerCase(),
                passwordEncoder.encode(rawPassword),
                fullName,
                role,
                dept,
                phone
            );
            userRepository.save(user);
            logger.info("Created initial account for role {} with email {}", role, email);
        }
    }
}

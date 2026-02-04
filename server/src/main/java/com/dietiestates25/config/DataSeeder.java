package com.dietiestates25.config;

import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgencyRepository;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    private final com.dietiestates25.repository.PropertyRepository propertyRepository;
    private final com.dietiestates25.repository.AgentRepository agentRepository; // Restored
    private final com.dietiestates25.repository.ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder; // Restored
    private final org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(
            new org.locationtech.jts.geom.PrecisionModel(), 4326);

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@dietiestates.com")) {
            User admin = new User();
            admin.setEmail("admin@dietiestates.com");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.getRoles().add(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("Default Admin account created: admin@dietiestates.com / admin");
        }

        User seededUser = null;
        if (!userRepository.existsByEmail("user@dieti.com")) {
            seededUser = new User();
            seededUser.setEmail("user@dieti.com");
            seededUser.setPasswordHash(passwordEncoder.encode("password"));
            seededUser.setFirstName("Regular");
            seededUser.setLastName("User");
            seededUser.getRoles().add(Role.USER);
            seededUser = userRepository.save(seededUser);
        } else {
            seededUser = userRepository.findByEmail("user@dieti.com").orElse(null);
        }

        // Seed Demo Agency and Agent if not present
        if (!userRepository.existsByEmail("manager@dieti.com")) {
            // Create Agency
            com.dietiestates25.model.Agency agency = new com.dietiestates25.model.Agency();
            agency.setName("DietiEstates Agency");
            agency.setAddress("Via Claudio 21, Napoli");
            agency.setPhone("0811234567");
            agency.setEmail("info@dieti.com");
            agency = agencyRepository.save(agency);

            // Create Manager
            User manager = new User();
            manager.setEmail("manager@dieti.com");
            manager.setFirstName("Mario");
            manager.setLastName("Manager");
            manager.setPasswordHash(passwordEncoder.encode("password"));
            manager.setAgency(agency);
            manager.getRoles().add(Role.AGENCY);
            userRepository.save(manager);

            // Create Agent
            com.dietiestates25.model.Agent agent = new com.dietiestates25.model.Agent();
            agent.setEmail("agent@dieti.com");
            agent.setFirstName("Luigi");
            agent.setLastName("Agent");
            agent.setPasswordHash(passwordEncoder.encode("password"));
            agent.setAgency(agency);
            agent.setBiography("Experienced agent at DietiEstates.");
            agent.setBirthDate(java.time.LocalDate.of(1985, 5, 20));
            agent.getRoles().add(Role.AGENT);
            userRepository.save(agent);

            // Create Another Agent (Mario Rossi) - ID 4
            com.dietiestates25.model.Agent mario = new com.dietiestates25.model.Agent();
            mario.setEmail("mario@dieti.com");
            mario.setFirstName("Mario");
            mario.setLastName("Rossi");
            mario.setPasswordHash(passwordEncoder.encode("password"));
            mario.setAgency(agency);
            mario.setBiography("Top agent at DietiEstates.");
            mario.setBirthDate(java.time.LocalDate.of(1990, 1, 15));
            mario.setPhoneNumber("+39 333 9999999");
            mario.getRoles().add(Role.AGENT);
            userRepository.save(mario);
        }

        // Check if properties need seeding (look for specific sample property)
        boolean seedProperties = propertyRepository.findAll().stream()
                .noneMatch(p -> "Modern Apartment in Naples".equals(p.getTitle()));

        if (seedProperties) {
            System.out.println("Seeding Properties...");
            // Fetch dependencies
            var agencyOpt = agencyRepository.findAll().stream().findFirst();
            if (agencyOpt.isPresent()) {
                var agency = agencyOpt.get();

                var agents = agentRepository.findAll();
                com.dietiestates25.model.Agent luigi = agents.stream()
                        .filter(a -> "agent@dieti.com".equals(a.getEmail())).findFirst().orElse(null);
                com.dietiestates25.model.Agent mario = agents.stream()
                        .filter(a -> "mario@dieti.com".equals(a.getEmail())).findFirst().orElse(null);

                if (luigi != null) {
                    // Seed Properties for Luigi
                    com.dietiestates25.model.Property p1 = new com.dietiestates25.model.Property();
                    p1.setTitle("Modern Apartment in Naples");
                    p1.setDescription("Beautiful apartment near the sea.");
                    p1.setPrice(new java.math.BigDecimal("250000.00"));
                    p1.setType(com.dietiestates25.model.PropertyType.SALE);
                    p1.setSizeSqm(85);
                    p1.setRooms(3);
                    p1.setFloor(2);
                    p1.setEnergyClass(com.dietiestates25.model.EnergyClass.A1);
                    p1.setAddress("Via Caracciolo 10");
                    p1.setCity("Napoli");
                    p1.setLocation(
                            geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(14.2281, 40.8327))); // Example
                                                                                                                      // coords
                    p1.setAgent(luigi);
                    p1.setAgency(agency);

                    com.dietiestates25.model.PropertyPhoto ph1 = new com.dietiestates25.model.PropertyPhoto();
                    ph1.setUrl("https://placehold.co/600x400?text=Modern+Apartment");
                    ph1.setProperty(p1);
                    p1.getPhotos().add(ph1);

                    propertyRepository.save(p1);

                    com.dietiestates25.model.Property p2 = new com.dietiestates25.model.Property();
                    p2.setTitle("Cozy Studio in Historic Center");
                    p2.setDescription("Perfect for students or singles.");
                    p2.setPrice(new java.math.BigDecimal("600.00"));
                    p2.setType(com.dietiestates25.model.PropertyType.RENT);
                    p2.setSizeSqm(40);
                    p2.setRooms(1);
                    p2.setFloor(1);
                    p2.setEnergyClass(com.dietiestates25.model.EnergyClass.D);
                    p2.setAddress("Via Tribunali 20");
                    p2.setCity("Napoli");
                    p2.setLocation(
                            geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(14.2563, 40.8518)));
                    p2.setAgent(luigi);
                    p2.setAgency(agency);

                    com.dietiestates25.model.PropertyPhoto ph2 = new com.dietiestates25.model.PropertyPhoto();
                    ph2.setUrl("https://placehold.co/600x400?text=Cozy+Studio");
                    ph2.setProperty(p2);
                    p2.getPhotos().add(ph2);

                    propertyRepository.save(p2);

                    // Reviews for Luigi
                    if (seededUser != null) {
                        var r1 = new com.dietiestates25.model.Review();
                        r1.setAgent(luigi);
                        r1.setUser(seededUser);
                        r1.setScore(5);
                        r1.setComment("Luigi was fantastic! Very helpful.");
                        reviewRepository.save(r1);
                    }
                }

                if (mario != null) {
                    // Seed Properties for Mario
                    com.dietiestates25.model.Property p3 = new com.dietiestates25.model.Property();
                    p3.setTitle("Luxury Villa with Pool");
                    p3.setDescription("Exclusive villa with panoramic view.");
                    p3.setPrice(new java.math.BigDecimal("850000.00"));
                    p3.setType(com.dietiestates25.model.PropertyType.SALE);
                    p3.setSizeSqm(200);
                    p3.setRooms(5);
                    p3.setFloor(0);
                    p3.setEnergyClass(com.dietiestates25.model.EnergyClass.A4);
                    p3.setAddress("Via Posillipo 50");
                    p3.setCity("Napoli");
                    p3.setLocation(
                            geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(14.2114, 40.8142)));
                    p3.setAgent(mario);
                    p3.setAgency(agency);

                    com.dietiestates25.model.PropertyPhoto ph3 = new com.dietiestates25.model.PropertyPhoto();
                    ph3.setUrl("https://placehold.co/600x400?text=Luxury+Villa");
                    ph3.setProperty(p3);
                    p3.getPhotos().add(ph3);

                    propertyRepository.save(p3);

                    // Reviews for Mario
                    if (seededUser != null) {
                        var r2 = new com.dietiestates25.model.Review();
                        r2.setAgent(mario);
                        r2.setUser(seededUser);
                        r2.setScore(4);
                        r2.setComment("Professional and punctual.");
                        reviewRepository.save(r2);
                    }
                }
            }
        }
    }
}

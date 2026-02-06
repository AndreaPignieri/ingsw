package com.dietiestates25.config;

import com.dietiestates25.model.Agency;
import com.dietiestates25.model.Agent;
import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.AgencyRepository;
import com.dietiestates25.repository.AgentRepository;
import com.dietiestates25.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (agencyRepository.count() > 0) {
            System.out.println("DataSeeder: Data already exists, skipping seeding.");
            return;
        }

        System.out.println("DataSeeder: Seeding initial data...");

        // 1. Prestige Living Milano
        createAgencyWithTeam(
                "Prestige Living Milano",
                "Via Alessandro Manzoni, 14, 20121 Milano MI",
                "info@prestigeliving.it",
                "+39 02 4567 8900",
                new ManagerData("Roberto", "Valenti", "r.valenti@prestigeliving.it", "AdminPrestige2024!"),
                new AgentData[] {
                        new AgentData("Giulia", "Martini", "1982-03-15",
                                "Specializzata in attici di lusso e palazzi storici. Con 12 anni di esperienza nel settore, offre consulenze per investimenti ad alto rendimento.",
                                "+39 333 1234567", "g.martini@prestigeliving.it", "Giulia#Realty88"),
                        new AgentData("Marco", "Ferrara", "1994-11-20",
                                "Dinamico ed esperto in locazioni brevi e appartamenti per professionisti. Appassionato di interior design e home staging.",
                                "+39 329 9876543", "m.ferrara@prestigeliving.it", "Marco_Agent02!")
                });

        // 2. Orizzonti Casa Roma
        createAgencyWithTeam(
                "Orizzonti Casa Roma",
                "Viale di Trastevere, 88, 00153 Roma RM",
                "contatti@orizzonticasa.com",
                "+39 06 1122 3344",
                new ManagerData("Elena", "Ricci", "elena.ricci@orizzonticasa.com", "Manager_Roma99"),
                new AgentData[] {
                        new AgentData("Luca", "Moretti", "1988-07-04",
                                "Esperto del territorio romano, accompagna le giovani coppie nell'acquisto della prima casa. Paziente e attento ai dettagli burocratici.",
                                "+39 347 5566778", "l.moretti@orizzonticasa.com", "LucaCasa$2024"),
                        new AgentData("Sara", "De Luca", "1979-01-12",
                                "Focalizzata su ville indipendenti e ristrutturazioni. Collabora strettamente con architetti per mostrare il potenziale nascosto di ogni immobile.",
                                "+39 320 4433221", "s.deluca@orizzonticasa.com", "Sara_Sales77")
                });

        // 3. Partenope Domus
        createAgencyWithTeam(
                "Partenope Domus",
                "Via dei Mille, 40, 80121 Napoli NA",
                "info@partenopedomus.it",
                "+39 081 555 1234",
                new ManagerData("Antonio", "Esposito", "a.esposito@partenopedomus.it", "Vesuvio_Boss80!"),
                new AgentData[] {
                        new AgentData("Assunta", "Russo", "1976-05-22",
                                "Esperta del Centro Storico e di investimenti turistici (B&B e case vacanza). Conosce ogni vicolo di Spaccanapoli e sa trovare vere perle nascoste.",
                                "+39 338 7654321", "a.russo@partenopedomus.it", "Spaccanapoli$24"),
                        new AgentData("Ciro", "Sorrentino", "1968-09-30",
                                "Specializzato in immobili di prestigio sulla collina di Posillipo e Chiaia. Il suo focus sono le ville con vista mare e appartamenti di alta rappresentanza.",
                                "+39 339 1122334", "c.sorrentino@partenopedomus.it", "Marechiaro#1"),
                        new AgentData("Federica", "Gallo", "1999-02-14",
                                "Giovane e focalizzata sugli affitti per studenti e lavoratori fuori sede nella zona ospedaliera e Fuorigrotta. Molto attiva sui social media.",
                                "+39 351 9988776", "f.gallo@partenopedomus.it", "Federica_Rent90")
                });

        System.out.println("DataSeeder: Seeding completed successfully.");
    }

    private void createAgencyWithTeam(String agencyName, String address, String email, String phone,
            ManagerData managerData, AgentData[] agents) {
        // Create Agency
        Agency agency = new Agency();
        agency.setName(agencyName);
        agency.setAddress(address);
        agency.setEmail(email);
        agency.setPhone(phone);
        agency = agencyRepository.save(agency);

        // Create Manager
        User manager = new User();
        manager.setFirstName(managerData.firstName);
        manager.setLastName(managerData.lastName);
        manager.setEmail(managerData.email);
        manager.setPasswordHash(passwordEncoder.encode(managerData.password));
        manager.setAgency(agency);
        manager.getRoles().add(Role.AGENCY); // Manager role
        manager.setIsActive(true);
        userRepository.save(manager);

        // Create Agents
        for (AgentData agentData : agents) {
            Agent agent = new Agent();
            agent.setFirstName(agentData.firstName);
            agent.setLastName(agentData.lastName);
            agent.setEmail(agentData.email);
            agent.setPasswordHash(passwordEncoder.encode(agentData.password));
            agent.setBirthDate(LocalDate.parse(agentData.birthDate));
            agent.setBiography(agentData.bio);
            agent.setPhoneNumber(agentData.phone);
            agent.setAgency(agency);
            agent.getRoles().add(Role.AGENT);
            agent.setIsActive(true);
            agentRepository.save(agent);
        }
    }

    // Helper records for structured data
    record ManagerData(String firstName, String lastName, String email, String password) {
    }

    record AgentData(String firstName, String lastName, String birthDate, String bio, String phone, String email,
            String password) {
    }
}

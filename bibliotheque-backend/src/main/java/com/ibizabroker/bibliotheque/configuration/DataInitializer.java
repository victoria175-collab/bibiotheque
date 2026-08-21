package com.ibizabroker.bibliotheque.configuration;

import com.ibizabroker.bibliotheque.dao.RoleRepository;
import com.ibizabroker.bibliotheque.dao.BooksRepository;
import com.ibizabroker.bibliotheque.dao.BorrowRepository;
import com.ibizabroker.bibliotheque.dao.ReservationRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Books;
import com.ibizabroker.bibliotheque.entity.Borrow;
import com.ibizabroker.bibliotheque.entity.Role;
import com.ibizabroker.bibliotheque.entity.Reservation;
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
import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BooksRepository booksRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private ReservationRepository reservationRepository;

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

        seedReservationScenario(allRoles);
    }

    private void seedReservationScenario(Set<Role> allRoles) {
        if (booksRepository.findByBookName("L1").isPresent()) {
            return;
        }

        Role userRole = roleRepository.findByRoleName("User").orElseThrow();
        Users a1 = createUser("A1", "Adhérent A1", userRole);
        Users a2 = createUser("A2", "Adhérent A2", userRole);
        Users a3 = createUser("A3", "Emprunteur A3", userRole);

        Books l1 = createBook("L1", 1);
        Books l2 = createBook("L2", 1);
        Books l3 = createBook("L3", 1);
        Books l4 = createBook("L4", 1);
        Books l5 = createBook("L5", 1);

        Date issueDate = new Date();
        saveBorrow(l2, a3, issueDate);
        saveBorrow(l3, a3, issueDate);
        saveBorrow(l4, a3, issueDate);
        saveBorrow(l5, a3, issueDate);

        saveReservation(l2, a1);
        saveReservation(l3, a2);
        saveReservation(l4, a2);
        saveReservation(l5, a2);
    }

    private Users createUser(String username, String name, Role role) {
        Users user = new Users();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(username.toLowerCase() + "48"));
        user.setRole(new HashSet<>(Set.of(role)));
        return usersRepository.save(user);
    }

    private Books createBook(String name, int copies) {
        Books book = new Books();
        book.setBookName(name);
        book.setBookAuthor("Auteur " + name);
        book.setBookGenre("Test");
        book.setNoOfCopies(copies);
        return booksRepository.save(book);
    }

    private void saveBorrow(Books book, Users user, Date issueDate) {
        book.borrowBook();
        booksRepository.save(book);

        Borrow borrow = new Borrow();
        borrow.setBookId(book.getBookId());
        borrow.setUserId(user.getUserId());
        borrow.setIssueDate(issueDate);
        borrowRepository.save(borrow);
    }

    private void saveReservation(Books book, Users user) {
        Reservation reservation = new Reservation();
        reservation.setLivre(book);
        reservation.setAdherent(user);
        reservationRepository.save(reservation);
    }
}

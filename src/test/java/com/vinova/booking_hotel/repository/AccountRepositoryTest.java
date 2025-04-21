package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        entityManager.clear();

        LocalDateTime pastLogin = LocalDateTime.now().minusDays(3);
        LocalDateTime recentLogin = LocalDateTime.now().minusHours(1);

        Account testAccount1 = new Account(null, "user1", "pass1", "user1@example.com", "User One", null, "111", null, recentLogin, null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);
        Account testAccount2 = new Account(null, "user2", "pass2", "user2@example.com", "User Two", null, "222", null, pastLogin, null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);
        Account testAccount3 = new Account(null, "admin1", "adminpass", "admin1@example.com", "Admin One", null, "333", null, recentLogin, null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);

        Role userRole = new Role(null, "USER", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());
        Role adminRole = new Role(null, "ADMIN", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());

        entityManager.persist(userRole);
        entityManager.persist(adminRole);
        entityManager.persist(testAccount1);
        entityManager.persist(testAccount2);
        entityManager.persist(testAccount3);

        AccountRole userRole1 = new AccountRole(null, testAccount1, userRole, ZonedDateTime.now(), ZonedDateTime.now());
        AccountRole userRole2 = new AccountRole(null, testAccount2, userRole, ZonedDateTime.now(), ZonedDateTime.now());
        AccountRole adminRole1 = new AccountRole(null, testAccount3, adminRole, ZonedDateTime.now(), ZonedDateTime.now());

        entityManager.persist(userRole1);
        entityManager.persist(userRole2);
        entityManager.persist(adminRole1);

        entityManager.flush();
    }

    @Test
    void findByUsername_shouldReturnAccount_whenUsernameExists() {
        Optional<Account> foundAccount = accountRepository.findByUsername("user1");
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getUsername()).isEqualTo("user1");
    }

    @Test
    void findByUsername_shouldReturnEmptyOptional_whenUsernameDoesNotExist() {
        Optional<Account> foundAccount = accountRepository.findByUsername("nonexistent");
        assertThat(foundAccount).isEmpty();
    }

    @Test
    void findByEmail_shouldReturnAccount_whenEmailExists() {
        Optional<Account> foundAccount = accountRepository.findByEmail("user2@example.com");
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    void findByEmail_shouldReturnEmptyOptional_whenEmailDoesNotExist() {
        Optional<Account> foundAccount = accountRepository.findByEmail("nonexistent@example.com");
        assertThat(foundAccount).isEmpty();
    }

    @Test
    void findByLatestLoginBefore_shouldReturnAccounts_withLatestLoginBeforeGivenTime() {
        List<Account> accounts = accountRepository.findByLatestLoginBefore(LocalDateTime.now().minusDays(1));
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().getUsername()).isEqualTo("user2");
    }

    @Test
    void findByLatestLoginBefore_shouldReturnEmptyList_whenNoAccountsMatchCriteria() {
        LocalDateTime fourDaysAgo = LocalDateTime.now(ZoneId.of("Asia/Saigon")).minusDays(4);
        List<Account> accounts = accountRepository.findByLatestLoginBefore(fourDaysAgo);
        assertThat(accounts).isEmpty();
    }

    @Test
    void findByAccountRoles_RoleName_shouldReturnAccounts_withGivenRoleName() {
        List<Account> userAccounts = accountRepository.findByAccountRoles_RoleName("USER");
        assertThat(userAccounts).hasSize(2);
        assertThat(userAccounts.get(0).getUsername()).isIn("user1", "user2");
        assertThat(userAccounts.get(1).getUsername()).isIn("user1", "user2");

        List<Account> adminAccounts = accountRepository.findByAccountRoles_RoleName("ADMIN");
        assertThat(adminAccounts).hasSize(1);
        assertThat(adminAccounts.getFirst().getUsername()).isEqualTo("admin1");
    }

    @Test
    void findByAccountRoles_RoleName_shouldReturnEmptyList_whenNoAccountsHaveGivenRole() {
        List<Account> accounts = accountRepository.findByAccountRoles_RoleName("GUEST");
        assertThat(accounts).isEmpty();
    }
}
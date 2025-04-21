package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.OwnerRegistration;
import com.vinova.booking_hotel.authentication.repository.OwnerRegistrationRepository;
import com.vinova.booking_hotel.common.enums.OwnerRegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class OwnerRegistrationRepositoryTest {

    @Autowired
    private OwnerRegistrationRepository ownerRegistrationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        // Xóa dữ liệu cũ
        ownerRegistrationRepository.deleteAll();
        entityManager.clear();

        // Chuẩn bị dữ liệu test, đảm bảo các trường NOT NULL được gán giá trị
        testAccount1 = new Account();
        testAccount1.setUsername("testowner1");
        testAccount1.setEmail("testowner1@example.com");
        testAccount1.setFullName("Test Owner One"); // Thêm giá trị cho fullName
        entityManager.persist(testAccount1);

        testAccount2 = new Account();
        testAccount2.setUsername("testowner2");
        testAccount2.setEmail("testowner2@example.com");
        testAccount2.setFullName("Test Owner Two"); // Thêm giá trị cho fullName
        entityManager.persist(testAccount2);

        OwnerRegistration testRegistrationPending1 = new OwnerRegistration();
        testRegistrationPending1.setAccount(testAccount1);
        testRegistrationPending1.setStatus(OwnerRegistrationStatus.PENDING);
        entityManager.persist(testRegistrationPending1);

        OwnerRegistration testRegistrationApproved1 = new OwnerRegistration();
        testRegistrationApproved1.setAccount(testAccount1);
        testRegistrationApproved1.setStatus(OwnerRegistrationStatus.ACCEPTED);
        entityManager.persist(testRegistrationApproved1);

        entityManager.flush();
    }

    @Test
    void findByAccountAndStatus_shouldReturnOwnerRegistration_whenMatchingAccountAndStatusExist() {
        Optional<OwnerRegistration> foundRegistration = ownerRegistrationRepository.findByAccountAndStatus(testAccount1, OwnerRegistrationStatus.PENDING);
        assertTrue(foundRegistration.isPresent());
        assertEquals(testAccount1.getId(), foundRegistration.get().getAccount().getId());
        assertEquals(OwnerRegistrationStatus.PENDING, foundRegistration.get().getStatus());
    }

    @Test
    void findByAccountAndStatus_shouldReturnEmptyOptional_whenMatchingAccountExistsButStatusDoesNotMatch() {
        Optional<OwnerRegistration> foundRegistration = ownerRegistrationRepository.findByAccountAndStatus(testAccount1, OwnerRegistrationStatus.REJECTED);
        assertFalse(foundRegistration.isPresent());
    }

    @Test
    void findByAccountAndStatus_shouldReturnEmptyOptional_whenMatchingStatusExistsButAccountDoesNotExist() {
        // Arrange
        Account nonExistentAccount = new Account();
        nonExistentAccount.setUsername("nonexistent");
        nonExistentAccount.setEmail("nonexistent@example.com");
        nonExistentAccount.setFullName("Non Existent User");
        entityManager.persist(nonExistentAccount); // Persist đối tượng nonExistentAccount
        entityManager.flush();

        // Act
        Optional<OwnerRegistration> foundRegistration = ownerRegistrationRepository.findByAccountAndStatus(nonExistentAccount, OwnerRegistrationStatus.PENDING);

        // Assert
        assertFalse(foundRegistration.isPresent());
    }

    @Test
    void findByAccountAndStatus_shouldReturnEmptyOptional_whenNoMatchingRecordExists() {
        Optional<OwnerRegistration> foundRegistration = ownerRegistrationRepository.findByAccountAndStatus(testAccount2, OwnerRegistrationStatus.PENDING);
        assertFalse(foundRegistration.isPresent());
    }
}
package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        entityManager.clear();

        Role userRole = new Role(null, "ROLE_USER", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());
        Role adminRole = new Role(null, "ROLE_ADMIN", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());
        Role ownerRole = new Role(null, "ROLE_OWNER", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());

        entityManager.persist(userRole);
        entityManager.persist(adminRole);
        entityManager.persist(ownerRole);

        entityManager.flush();
    }

    @Test
    void findByName_shouldReturnRole_whenRoleExistsWithName() {
        Optional<Role> foundRole = roleRepository.findByName("ROLE_USER");
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void findByName_shouldReturnEmptyOptional_whenRoleDoesNotExistWithName() {
        Optional<Role> foundRole = roleRepository.findByName("ROLE_GUEST");
        assertThat(foundRole).isEmpty();
    }

    @Test
    void save_shouldPersistNewRole() {
        Role newRole = new Role(null, "ROLE_TEST", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>());
        Role savedRole = roleRepository.save(newRole);
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("ROLE_TEST");

        Optional<Role> retrievedRole = Optional.ofNullable(entityManager.find(Role.class, savedRole.getId()));
        assertThat(retrievedRole).isPresent();
        assertThat(retrievedRole.get().getName()).isEqualTo("ROLE_TEST");
    }

    @Test
    void findById_shouldReturnRole_whenRoleExistsWithId() {
        Role existingRole = entityManager.persist(new Role(null, "ROLE_EXISTING", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>()));
        entityManager.flush();

        Optional<Role> foundRole = roleRepository.findById(existingRole.getId());
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ROLE_EXISTING");
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenRoleDoesNotExistWithId() {
        Optional<Role> foundRole = roleRepository.findById(999L);
        assertThat(foundRole).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllPersistedRoles() {
        List<Role> allRoles = roleRepository.findAll();
        assertThat(allRoles).hasSize(3); // Dựa trên dữ liệu setUp
        assertThat(allRoles).extracting("name").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_OWNER");
    }

    @Test
    void delete_shouldRemoveRoleFromDatabase() {
        Role roleToDelete = entityManager.persist(new Role(null, "ROLE_DELETE_ME", ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>()));
        entityManager.flush();
        Long roleIdToDelete = roleToDelete.getId();

        roleRepository.delete(roleToDelete);
        entityManager.flush();

        Optional<Role> deletedRole = Optional.ofNullable(entityManager.find(Role.class, roleIdToDelete));
        assertThat(deletedRole).isEmpty();
    }
}
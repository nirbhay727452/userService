package dev.nirbhay.userservice.secutiry.repositories;

import java.util.Optional;


import dev.nirbhay.userservice.secutiry.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}

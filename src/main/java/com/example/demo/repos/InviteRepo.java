package com.example.demo.repos;

import com.example.demo.models.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface InviteRepo extends JpaRepository<Invitation, Long> {
    List<Invitation> findByEmailAndStatus(String email, String status);

}

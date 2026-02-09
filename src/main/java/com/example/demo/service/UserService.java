package com.example.demo.service;

import com.example.demo.models.User;
import com.example.demo.models.Organization;
import com.example.demo.models.Invitation;
import com.example.demo.repos.UserRepo;
import com.example.demo.repos.OrgRepo;
import com.example.demo.repos.InviteRepo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepo userRepo;
    private final OrgRepo orgRepo;
    private final InviteRepo inviteRepo;
    
    public UserService(UserRepo userRepo, OrgRepo orgRepo, InviteRepo inviteRepo) {
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
        this.inviteRepo = inviteRepo;
    }
    
    public Optional<Organization> getOrganization(Long userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent() && user.get().getOrganizationId() != null) {
            return orgRepo.findById(user.get().getOrganizationId());
        }
        return Optional.empty();
    }
    
    public List<Invitation> getInvitationsForEmail(String email) {
        return inviteRepo.findByEmailAndStatus(email, "PENDING");
    }
    
    public Organization acceptInvitation(Long invitationId, String email) {
        Invitation invite = inviteRepo.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        if (!email.equals(invite.getEmail())) {
            throw new RuntimeException("Invitation not for this email");
        }
        
        if (!"PENDING".equals(invite.getStatus())) {
            throw new RuntimeException("Invitation already used");
        }
        
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getOrganizationId() != null) {
            throw new RuntimeException("User already in an organization");
        }
        
        Organization org = orgRepo.findById(invite.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        user.setOrganizationId(org.getId());
        userRepo.save(user);
        
        invite.setStatus("ACCEPTED");
        inviteRepo.save(invite);
        
        return org;
    }
}



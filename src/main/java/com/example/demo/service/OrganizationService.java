package com.example.demo.service;

import com.example.demo.models.User;
import com.example.demo.models.Organization;
import com.example.demo.models.Invitation;
import com.example.demo.repos.UserRepo;
import com.example.demo.repos.OrgRepo;
import com.example.demo.repos.InviteRepo;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;

@Service
public class OrganizationService {
    
    private final OrgRepo orgRepo;
    private final UserRepo userRepo;
    private final InviteRepo inviteRepo;
    
    public OrganizationService(OrgRepo orgRepo, UserRepo userRepo, InviteRepo inviteRepo) {
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.inviteRepo = inviteRepo;
    }
    
    public Organization createOrganization(String orgName, String adminEmail) {
        User admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (admin.getOrganizationId() != null) {
            throw new RuntimeException("User already belongs to an organization");
        }
        
        Organization org = new Organization();
        org.setName(orgName);
        org.setAdminId(admin.getId());
        org = orgRepo.save(org);
        
        admin.setOrganizationId(org.getId());
        userRepo.save(admin);
        
        return org;
    }
    
    public Invitation inviteUser(Long orgId, String emailToInvite, String inviterEmail) {
        User inviter = userRepo.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));
        
        Organization org = orgRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (!org.getAdminId().equals(inviter.getId())) {
            throw new RuntimeException("Only admin can send invitations");
        }
        
        Invitation invite = new Invitation();
        invite.setEmail(emailToInvite);
        invite.setToken(UUID.randomUUID().toString());
        invite.setOrganizationId(org.getId());
        invite.setInvitedById(inviter.getId());
        invite.setStatus("PENDING");
        
        return inviteRepo.save(invite);
    }
    
    public List<User> getOrganizationMembers(Long orgId) {
        return userRepo.findByOrganizationId(orgId);
    }
}

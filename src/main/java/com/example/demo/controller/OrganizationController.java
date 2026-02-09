package com.example.demo.controller;

import com.example.demo.models.Organization;
import com.example.demo.models.User;
import com.example.demo.models.Invitation;
import com.example.demo.service.OrganizationService;
import com.example.demo.security.JwtUtil;
import com.example.demo.repos.UserRepo;
import com.example.demo.repos.OrgRepo;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orgs")
@CrossOrigin(origins = "*")
public class OrganizationController {
    
    private final OrganizationService orgService;
    private final UserRepo userRepo;
    private final OrgRepo orgRepo;
    private final JwtUtil jwtUtil;
    
    public OrganizationController(OrganizationService orgService, UserRepo userRepo, OrgRepo orgRepo, JwtUtil jwtUtil) {
        this.orgService = orgService;
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
        this.jwtUtil = jwtUtil;
    }
    
    private String getCurrentEmail(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.getEmailFromToken(auth.substring(7));
        }
        throw new RuntimeException("Unauthorized");
    }
    
    @PostMapping
    public Map<String, Object> createOrganization(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String email = getCurrentEmail(request);
        Organization org = orgService.createOrganization(body.get("name"), email);
        User admin = userRepo.findById(org.getAdminId()).orElse(null);
        
        return Map.of(
            "id", org.getId(),
            "name", org.getName(),
            "adminId", org.getAdminId(),
            "adminEmail", admin != null ? admin.getEmail() : ""
        );
    }
    
    @PostMapping("/{id}/invite")
    public Map<String, Object> inviteUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String email = getCurrentEmail(request);
        Invitation invite = orgService.inviteUser(id, body.get("email"), email);
        
        return Map.of(
            "id", invite.getId(),
            "email", invite.getEmail(),
            "organizationId", invite.getOrganizationId(),
            "status", invite.getStatus()
        );
    }
    
    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getMembers(@PathVariable Long id) {
        return orgService.getOrganizationMembers(id).stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("email", u.getEmail());
                    return map;
                })
                .collect(Collectors.toList());
    }
}

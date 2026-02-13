package com.example.demo.controller;

import com.example.demo.models.User;
import com.example.demo.models.Organization;
import com.example.demo.models.Invitation;
import com.example.demo.service.UserService;
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
@RequestMapping("/user")

public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final OrgRepo orgRepo;

    public UserController(UserService userService, JwtUtil jwtUtil, UserRepo userRepo, OrgRepo orgRepo) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
    }

    private String getCurrentEmail(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.getEmailFromToken(auth.substring(7));
        }
        throw new RuntimeException("Unauthorized");
    }

    @GetMapping("/me")
    public Map<String, Object> getMe(HttpServletRequest request) {
        String email = getCurrentEmail(request);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());

        if (user.getOrganizationId() != null) {
            Organization org = orgRepo.findById(user.getOrganizationId()).orElse(null);
            if (org != null) {
                List<User> members = userRepo.findByOrganizationId(org.getId());
                User admin = userRepo.findById(org.getAdminId()).orElse(null);

                Map<String, Object> orgData = new java.util.HashMap<>();
                orgData.put("id", org.getId());
                orgData.put("name", org.getName());
                orgData.put("admin", admin != null ? Map.of("id", admin.getId(), "email", admin.getEmail()) : null);
                orgData.put("members", members.stream()
                        .map(m -> Map.of("id", m.getId(), "email", m.getEmail()))
                        .collect(Collectors.toList()));

                result.put("organization", orgData);
            }
        }

        return result;
    }

    @GetMapping("/invites")
    public Map<String, Object> getInvites(HttpServletRequest request) {
        String email = getCurrentEmail(request);
        List<Invitation> invites = userService.getInvitationsForEmail(email);

        List<Map<String, Object>> inviteList = invites.stream().map(inv -> {
            Organization org = orgRepo.findById(inv.getOrganizationId()).orElse(null);
            User inviter = userRepo.findById(inv.getInvitedById()).orElse(null);

            Map<String, Object> map = new HashMap<>();
            map.put("id", inv.getId());
            map.put("email", inv.getEmail());
            map.put("organizationId", inv.getOrganizationId());
            map.put("organizationName", org != null ? org.getName() : "");
            map.put("invitedBy", inviter != null ? inviter.getEmail() : "");
            map.put("status", inv.getStatus());
            map.put("createdAt", inv.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("invitations", inviteList);
        return response;
    }

    @PostMapping("/invites/{inviteId}/accept")
    public Map<String, Object> acceptInvite(
            @PathVariable Long inviteId,
            HttpServletRequest request) {
        String email = getCurrentEmail(request);
        Organization org = userService.acceptInvitation(inviteId, email);

        return Map.of(
                "id", org.getId(),
                "name", org.getName(),
                "message", "Successfully joined organization");
    }
}

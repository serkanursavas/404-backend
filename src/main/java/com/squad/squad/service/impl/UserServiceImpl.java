package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.squad.squad.dto.user.*;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.mapper.UserMapper;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import com.squad.squad.service.TenantContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.squad.squad.dto.PlayerDTO;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.User;
import com.squad.squad.exception.UserNotFoundException;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.service.PlayerService;
import com.squad.squad.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerService playerService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final GroupMembershipRepository membershipRepository;
    private final TenantContextService tenantContextService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PlayerService playerService, AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            UserMapper userMapper, GroupMembershipRepository membershipRepository,
            TenantContextService tenantContextService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.membershipRepository = membershipRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();
            String userRole = userDetails.getRole();

            // Kullanıcının grup ID'sini al ve TenantContext'i ayarla
            Integer userGroupId = userDetails.getGroupId();
            if ("ROLE_ADMIN".equals(userRole)) {
                // Süper admin ise, RLS'yi atlamak için 0'ı kullan (PostgreSQL'de 'SUPER_ADMIN'
                // olarak ayarlanacak)
                tenantContextService.setSuperAdminContext(); // setSuperAdminContext çağrıldı
            } else if (userGroupId != null) {
                // Normal kullanıcı veya grup yöneticisi ise, kendi grup ID'sini kullan
                tenantContextService.setTenantContext(userGroupId); // setTenantContext çağrıldı
            } else {
                // Grup ID'si null ise (veya pending durumdaysa), varsayılan olarak 0'ı kullan
                tenantContextService.setTenantContext(0); // Varsayılan veya pending grubu için 0
            }

            return jwtUtils.generateToken(userId, username,
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(",")));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    public String createUser(UserCreateRequestDTO user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        User savedUser = new User();
        savedUser.setUsername(user.getUsername().toLowerCase());
        savedUser.setPassword(encodedPassword);
        savedUser.setGroupId(0); // Pending group

        Player player = new Player();
        player.setName(user.getPlayerCreateDTO().getName());
        player.setSurname(user.getPlayerCreateDTO().getSurname());
        player.setPosition(user.getPlayerCreateDTO().getPosition());
        player.setFoot(user.getPlayerCreateDTO().getFoot());
        player.setGroupId(0); // Pending group

        player.setUser(savedUser);
        savedUser.setPlayer(player);

        userRepository.save(savedUser);

        return jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @Override
    public List<GetAllUsersDTO> getAllUsers() {
        CustomUserDetails currentUser = getCurrentUser();
        Integer currentUserGroupId = currentUser.getGroupId();

        List<User> users;
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            // Super Admin tüm kullanıcıları görür.
            users = userRepository.findAll();
        } else {
            if (currentUserGroupId == 0) {
                // Kullanıcı henüz onay bekliyor, sadece kendini görür
                User currentUserEntity = userRepository.findById(currentUser.getId()).orElse(null);
                if (currentUserEntity != null) {
                    users = List.of(currentUserEntity);
                } else {
                    users = new ArrayList<>();
                }
            } else {
                // Grup yöneticisi veya normal kullanıcı ise, sadece kendi grubundaki
                // kullanıcıları getir
                users = userRepository.findAllByGroupId(currentUserGroupId);
            }
        }

        List<GetAllUsersDTO> userDTOs = userMapper.usersToGetAllUsersDTOs(users);

        // Her bir DTO için groupRole ve groupId değerlerini doldur
        for (GetAllUsersDTO dto : userDTOs) {
            // Kullanıcının kendi grubundaki üyeliğini bul (adminGroupId yerine dto'nun
            // kendi groupId'si)
            // Eğer süper admin ise ve RLS'yi atlıyorsak, o kullanıcının ana grubunu
            // kullanırız.
            Integer targetUserGroupId = dto.getGroupId();

            // Süper admin durumu: Kendi grubunda olmayan kullanıcılar için groupRole boş
            // bırakılabilir
            // veya sistemdeki ilgili group_membership bilgisi çekilebilir.
            // Şimdilik, eğer current user admin ise ve target user'ın kendi group_id'si
            // varsa o group_id ile sorgula.
            // Değilse, current user'ın group_id'si ile sorgula.
            Integer effectiveGroupIdForMembership = ("ROLE_ADMIN".equals(currentUser.getRole())
                    && targetUserGroupId != null)
                            ? targetUserGroupId
                            : currentUserGroupId;

            if (effectiveGroupIdForMembership != null && effectiveGroupIdForMembership != 0) { // Grup ID 0 ise pending
                                                                                               // user veya super admin
                                                                                               // context'i
                membershipRepository.findByUserIdAndGroupIdAndStatus(
                        dto.getId(),
                        effectiveGroupIdForMembership,
                        GroupMembership.MembershipStatus.APPROVED)
                        .ifPresent(membership -> dto.setGroupRole(membership.getRole().name()));
            } else if (effectiveGroupIdForMembership == 0) {
                // Eğer kullanıcı pending ise (groupId == 0), grup rolü yoktur, boş bırak
                dto.setGroupRole(null);
            }
        }
        return userDTOs;
    }

    private CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void updateUser(String username, UserUpdateRequestDTO updatedUser) {

        // SecurityContext'ten kullanıcı bilgilerini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // Token içinden kullanıcı adını alır

        if (!currentUsername.equals(username.toLowerCase())) {
            throw new InvalidCredentialsException("You can only update your own profile.");
        }

        User existingUser = getUserByUsername(username);

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(updatedUser.getPassword());
            existingUser.setPassword(encodedPassword);
        }

        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        if (username != null) {
            User user = getUserByUsername(username);
            PlayerDTO player = playerService.getPlayerById(user.getId());

            playerService.softDelete(player);
            userRepository.deleteByUsername(username);
        } else {
            throw new IllegalArgumentException("Userame must be provided.");
        }
    }

    @Override
    public String resetPassword(String username) {
        User user = getUserByUsername(username);

        String encodedPassword = passwordEncoder.encode("reset" + user.getUsername());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return "Password reset to reset" + username + " successfully for user: " + username;
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public void updateUserRole(String username, UserRoleUpdateRequestDTO roleDTO) {
        // Hedef kullanıcıyı bul
        User targetUser = getUserByUsername(username);

        // Mevcut kullanıcının (isteği yapan adminin) group_id'sini al
        // Bu group_id, RLS tarafından filtrelenen verinin scope'unu belirler.
        // Bu nedenle, adminin sadece kendi grubundaki kullanıcıların rollerini
        // değiştirebilmesi gerekir.
        CustomUserDetails currentUser = getCurrentUser();
        Integer adminGroupId = currentUser.getGroupId();

        // Hedef kullanıcının kendi grubundaki GroupMembership kaydını bul
        GroupMembership groupMembership = membershipRepository
                .findByUserIdAndGroupIdAndStatus(targetUser.getId(), adminGroupId,
                        GroupMembership.MembershipStatus.APPROVED)
                .orElseThrow(() -> new RuntimeException(
                        "Group membership not found or not approved for user in current group."));

        // group_memberships.role alanını güncelle
        groupMembership.setRole(GroupMembership.MembershipRole.valueOf(roleDTO.getRole().toUpperCase()));
        membershipRepository.save(groupMembership);

        // user.role alanını bu UI üzerinden güncellemiyoruz.
        // user.setRole(roleDTO.getRole().toUpperCase()); // Bu satır
        // kaldırıldı/yorumlandı
        // userRepository.save(targetUser); // Sadece groupMembership güncellendiği için
        // bu kaydetme işlemi de gerekli değil
    }

    @Override
    public boolean isGroupAdmin(Integer userId) {
        return membershipRepository.existsByUserIdAndStatusAndRole(
                userId,
                GroupMembership.MembershipStatus.APPROVED,
                GroupMembership.MembershipRole.GROUP_ADMIN);
    }
}
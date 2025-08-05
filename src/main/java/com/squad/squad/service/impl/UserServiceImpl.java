package com.squad.squad.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.squad.squad.dto.user.GetAllUsersDTO;
import com.squad.squad.dto.user.UserCreateRequestDTO;
import com.squad.squad.dto.user.UserRoleUpdateRequestDTO;
import com.squad.squad.dto.user.UserUpdateRequestDTO;
import com.squad.squad.entity.GroupMembership;
import com.squad.squad.entity.Player;
import com.squad.squad.entity.User;
import com.squad.squad.exception.InvalidCredentialsException;
import com.squad.squad.exception.UserNotFoundException;
import com.squad.squad.mapper.UserMapper;
import com.squad.squad.repository.GroupMembershipRepository;
import com.squad.squad.repository.UserRepository;
import com.squad.squad.security.CustomUserDetails;
import com.squad.squad.security.JwtUtils;
import com.squad.squad.service.GroupService;
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
    private final GroupService groupService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PlayerService playerService, AuthenticationManager authenticationManager, JwtUtils jwtUtils,
            UserMapper userMapper, GroupMembershipRepository membershipRepository, GroupService groupService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerService = playerService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
        this.membershipRepository = membershipRepository;
        this.groupService = groupService;
    }

    @Override
    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.toLowerCase(), password));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer userId = userDetails.getId();
            String userRole = userDetails.getRole();

            // Kullanıcının onaylanmış grup ID'sini al
            Integer approvedGroupId = null;
            if (!"ROLE_ADMIN".equals(userRole)) {
                // Onaylanmış üyelik var mı kontrol et
                List<GroupMembership> approvedMemberships = membershipRepository.findByUserIdAndStatus(
                        userId, GroupMembership.MembershipStatus.APPROVED);

                if (!approvedMemberships.isEmpty()) {
                    // İlk onaylanmış üyeliği al
                    approvedGroupId = approvedMemberships.get(0).getGroupId();
                }
            }

            return jwtUtils.generateToken(userId, username,
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(",")),
                    approvedGroupId); // Sadece onaylanmış group_id'yi JWT'ye ekle
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public String createUserWithGroup(UserCreateRequestDTO user) {
        // Eğer grup bilgileri varsa otomatik olarak grup oluşturma isteği yap
        if (user.getGroupName() != null && !user.getGroupName().trim().isEmpty()) {
            // Önce kullanıcıyı oluştur
            String token = createUser(user);

            // Kullanıcıyı bul
            User createdUser = userRepository.findByUsername(user.getUsername().toLowerCase())
                    .orElseThrow(() -> new RuntimeException("Oluşturulan kullanıcı bulunamadı."));

            // Grup oluşturma isteği oluştur
            try {
                com.squad.squad.dto.group.GroupCreateRequestDTO groupRequest = new com.squad.squad.dto.group.GroupCreateRequestDTO();
                groupRequest.setGroupName(user.getGroupName());
                groupRequest.setGroupDescription(user.getGroupDescription());
                groupRequest.setIntendedAdminUserId(createdUser.getId());

                groupService.createGroupRequest(groupRequest);

                return token;
            } catch (Exception e) {
                // Grup oluşturma başarısız olursa kullanıcıyı sil
                userRepository.delete(createdUser);
                throw new RuntimeException(
                        "Kullanıcı oluşturuldu ama grup oluşturma başarısız oldu: " + e.getMessage());
            }
        } else {
            // Normal kullanıcı oluşturma
            return createUser(user);
        }
    }

    @Override
    public String createUser(UserCreateRequestDTO user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        User savedUser = new User();
        savedUser.setUsername(user.getUsername().toLowerCase());
        savedUser.setPassword(encodedPassword);
        savedUser.setGroupId(null); // Başlangıçta null - üyelik onaylandığında set edilecek

        Player player = new Player();
        player.setName(user.getPlayerCreateDTO().getName());
        player.setSurname(user.getPlayerCreateDTO().getSurname());
        player.setPosition(user.getPlayerCreateDTO().getPosition());
        player.setFoot(user.getPlayerCreateDTO().getFoot());
        player.setGroupId(null); // Başlangıçta null

        player.setUser(savedUser);
        savedUser.setPlayer(player);

        userRepository.save(savedUser);

        return jwtUtils.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    @Override
    public List<GetAllUsersDTO> getAllUsers() {
        CustomUserDetails currentUser = getCurrentUser();

        List<User> users;
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            // Super Admin tüm kullanıcıları görür.
            users = userRepository.findAll();
        } else {
            // Normal kullanıcı için onaylanmış üyelikleri kontrol et
            List<GroupMembership> approvedMemberships = membershipRepository.findByUserIdAndStatus(
                    currentUser.getId(), GroupMembership.MembershipStatus.APPROVED);

            if (approvedMemberships.isEmpty()) {
                // Onaylanmış üyelik yoksa sadece kendini görür
                User currentUserEntity = userRepository.findById(currentUser.getId()).orElse(null);
                if (currentUserEntity != null) {
                    users = List.of(currentUserEntity);
                } else {
                    users = new ArrayList<>();
                }
            } else {
                // Onaylanmış üyelik varsa, o grubun kullanıcılarını getir
                Integer approvedGroupId = approvedMemberships.get(0).getGroupId();
                users = userRepository.findAllByGroupId(approvedGroupId);
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
            // Değilse, current user'ın onaylanmış group_id'si ile sorgula.
            Integer effectiveGroupIdForMembership;
            if ("ROLE_ADMIN".equals(currentUser.getRole()) && targetUserGroupId != null) {
                effectiveGroupIdForMembership = targetUserGroupId;
            } else {
                // Normal kullanıcı için onaylanmış üyeliklerden group_id al
                List<GroupMembership> approvedMemberships = membershipRepository.findByUserIdAndStatus(
                        currentUser.getId(), GroupMembership.MembershipStatus.APPROVED);
                effectiveGroupIdForMembership = approvedMemberships.isEmpty() ? null
                        : approvedMemberships.get(0).getGroupId();
            }

            if (effectiveGroupIdForMembership != null) {
                membershipRepository.findByUserIdAndGroupIdAndStatus(
                        dto.getId(),
                        effectiveGroupIdForMembership,
                        GroupMembership.MembershipStatus.APPROVED)
                        .ifPresent(membership -> dto.setGroupRole(membership.getRole().name()));
            } else {
                // Eğer kullanıcının onaylanmış üyeliği yoksa, grup rolü yoktur, boş bırak
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

    @Override
    public boolean canUserAccessUserData(Object userPrincipal, String targetUsername) {
        if (!(userPrincipal instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails currentUser = (CustomUserDetails) userPrincipal;

        // Super Admin her zaman erişebilir
        if ("ROLE_ADMIN".equals(currentUser.getRole())) {
            return true;
        }

        // Kendi verilerine erişim
        if (currentUser.getUsername().equals(targetUsername)) {
            return true;
        }

        // Aynı grup üyelerine erişim kontrolü (Grup adminleri için)
        if (isGroupAdmin(currentUser.getId())) {
            try {
                User targetUser = getUserByUsername(targetUsername);
                return currentUser.getGroupId().equals(targetUser.getGroupId());
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

}
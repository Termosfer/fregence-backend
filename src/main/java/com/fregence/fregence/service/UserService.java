package com.fregence.fregence.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fregence.fregence.dto.ChangePasswordDTO;
import com.fregence.fregence.dto.UserResponseDTO;
import com.fregence.fregence.dto.UserUpdateDTO;
import com.fregence.fregence.entity.Order;
import com.fregence.fregence.entity.Role;
import com.fregence.fregence.entity.User;
import com.fregence.fregence.repository.CartRepository;
import com.fregence.fregence.repository.OrderRepository;
import com.fregence.fregence.repository.UserRepository;
import com.fregence.fregence.repository.WishlistRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class UserService {
	
    @Autowired
    private UserRepository userRepository;
	
    @Autowired
    private PasswordEncoder passwordEncoder;

    // YENİ: Digər dataları təmizləmək üçün repository-lər
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;
    
    public User register(User user) {
    	if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        user.setRole(Role.USER);

        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);

        savedUser.setPassword(rawPassword);
        return savedUser;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }
    
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
                .toList();
    }

    @Transactional
    public User updateProfile(UserUpdateDTO updateDTO) {
        User user = getCurrentUser();
        
        if (!user.getEmail().equals(updateDTO.getEmail()) && 
            userRepository.findByEmail(updateDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Bu email artıq başqa istifadəçi tərəfindən istifadə olunur!");
        }

        user.setName(updateDTO.getName());
        user.setEmail(updateDTO.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordDTO passwordDTO, PasswordEncoder passwordEncoder) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Köhnə şifrəniz yanlışdır!");
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    // YENİ: İsti̇fadəçi̇ni̇ si̇lmə metodu
 // YENİ: İsti̇fadəçi̇ni̇ si̇lmə metodu (Qorunmuş versiya)
    @Transactional
    public void deleteUser(Long id) {
        // 1. Hazırda daxil olan istifadəçini tapırıq
        User currentUser = getCurrentUser();

        // 2. QORUMA: Əgər silinmək istənən ID hazırkı adminin ID-sidirsə, imtina et
        if (currentUser.getId().equals(id)) {
            throw new RuntimeException("Öz hesabınızı silə bilməzsiniz! Bu sistemin kilidlənməsinə səbəb ola bilər.");
        }

        // 3. Silinəcək istifadəçini tapırıq
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        // 4. Sifariş tarixçəsini qoru (User referansını null et)
        List<Order> orders = orderRepository.findByUser(user);
        if (!orders.isEmpty()) {
            orders.forEach(order -> order.setUser(null));
            orderRepository.saveAll(orders);
        }

        // 5. Aktiv səbəti və wishlist-i sil
        cartRepository.deleteByUser(user);
        wishlistRepository.deleteByUser(user);

        // 6. İstifadəçini sil
        userRepository.delete(user);
    }
}
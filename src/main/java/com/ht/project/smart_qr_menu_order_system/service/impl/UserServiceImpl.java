package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.entity.RestaurantUser;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.mapper.UserMapper;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantUserRepository;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import com.ht.project.smart_qr_menu_order_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RestaurantUserRepository restaurantUserRepository;
    private final RestaurantAccessService restaurantAccessService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(String restaurantId, CreateUserRequest request, String ownerId) {
        Restaurant restaurant = restaurantAccessService.getRestaurantForOwner(restaurantId, ownerId);
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Email already exists");
        });

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CASHIER);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        RestaurantUser restaurantUser = RestaurantUser.builder()
                .restaurant(restaurant)
                .user(savedUser)
                .build();
        restaurantUser.setTenantId(restaurantId);
        restaurantUserRepository.save(restaurantUser);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String restaurantId, String id, String ownerId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, ownerId);
        RestaurantUser restaurantUser = restaurantUserRepository.findByRestaurantIdAndUserId(restaurantId, id)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND));

        if (restaurantUser.getUser().getRole() == Role.OWNER) {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Owner account cannot be removed from restaurant");
        }

        restaurantUserRepository.delete(restaurantUser);
        if (restaurantUserRepository.countByUserId(id) == 0) {
            userRepository.delete(restaurantUser.getUser());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String restaurantId, String ownerId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, ownerId);
        return restaurantUserRepository.findAllByRestaurantId(restaurantId)
                .stream()
                .map(RestaurantUser::getUser)
                .map(userMapper::toUserResponse)
                .toList();
    }
}

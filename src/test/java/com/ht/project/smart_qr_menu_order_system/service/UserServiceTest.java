package com.ht.project.smart_qr_menu_order_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.entity.RestaurantUser;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.mapper.UserMapper;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantUserRepository;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    RestaurantUserRepository restaurantUserRepository;

    @Mock
    RestaurantAccessService restaurantAccessService ; 

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserServiceImpl userService ; 

    @Test
    void testCreateUser_Success(){
      String restaurantId = "123" ;
      CreateUserRequest request = new CreateUserRequest("user@gmail.com", "123456", "0123456789", "Nguyen Van A");
      String ownerId = "1" ;

      Restaurant restaurant = new Restaurant() ;
      User user = new User() ; 
      User savedUser = new User() ; 
      RestaurantUser savedRestaurantUser = new RestaurantUser() ;
      UserResponse userResponse = mockResponse() ;
      when(restaurantAccessService.getRestaurantForOwner(restaurantId, ownerId)).thenReturn(restaurant) ;
      
      when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty()) ;

      when(userMapper.toUser(request)).thenReturn(user) ;
      when(passwordEncoder.encode(request.password())).thenReturn("encode") ;
      when(userRepository.save(user)).thenReturn(savedUser) ;

      when(restaurantUserRepository.save(any())).thenReturn(savedRestaurantUser) ;

      when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse) ;

      UserResponse result = userService.createUser(restaurantId, request, ownerId) ;

      assertNotNull(result);
      assertEquals(userResponse, result);
      assertEquals("encode", user.getPassword());
      assertEquals(Role.CASHIER, user.getRole());
      assertTrue(user.isActive());

      verify(userRepository).save(user);
      verify(restaurantUserRepository).save(any());
    }
    private UserResponse mockResponse() {
    return new UserResponse(
        "1", "Test", "0123", "email@test.com", "CASHIER", true
    );
}
}

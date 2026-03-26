package com.ht.project.smart_qr_menu_order_system.mapper;


import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toUser(CreateUserRequest request) ;

    UserResponse toUserResponse(User user) ;

}

package com.shopflow.user.service.impl;

import com.shopflow.user.dto.AddressRequest;
import com.shopflow.user.dto.AddressResponse;
import com.shopflow.user.entity.Address;
import com.shopflow.user.entity.User;
import com.shopflow.user.exception.ResourceNotFoundException;
import com.shopflow.user.repository.AddressRepository;
import com.shopflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void addAddress_shouldClearPreviousDefault_whenNewAddressIsDefault() {
        User user = new User();
        user.setId(1L);

        AddressRequest request = new AddressRequest(
                "101 Marine Drive", "Mumbai", "Maharashtra", "400002", "India", true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address a = invocation.getArgument(0);
            a.setId(10L);
            return a;
        });

        AddressResponse response = userService.addAddress(1L, request);

        verify(addressRepository).clearDefaultAddressForUser(1L);
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.isDefault()).isTrue();
    }

    @Test
    void deleteAddress_shouldThrowNotFound_whenAddressDoesNotBelongToUser() {
        when(addressRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAddress(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(addressRepository, never()).delete(any());
    }
}
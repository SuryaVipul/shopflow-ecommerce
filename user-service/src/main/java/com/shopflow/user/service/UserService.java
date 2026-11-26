package com.shopflow.user.service;

import com.shopflow.user.dto.AddressRequest;
import com.shopflow.user.dto.AddressResponse;
import com.shopflow.user.dto.UpdateProfileRequest;
import com.shopflow.user.dto.UserProfileResponse;

import java.util.List;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    List<AddressResponse> getAddresses(Long userId);

    AddressResponse addAddress(Long userId, AddressRequest request);

    void deleteAddress(Long userId, Long addressId);
}
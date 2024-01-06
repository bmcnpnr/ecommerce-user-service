package com.ecommerce.user.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    public static final String HELLO_FROM_USER_SERVICE = "Hello from User Service!";

    public String getHelloMessage() {
        return HELLO_FROM_USER_SERVICE;
    }
}

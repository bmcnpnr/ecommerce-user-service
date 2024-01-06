package com.ecommerce.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ecommerce.user.service.UserService.HELLO_FROM_USER_SERVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Test
    void testGetHelloMessage() {
        // when
        var result = userService.getHelloMessage();

        // then
        assertEquals(HELLO_FROM_USER_SERVICE, result);
    }
}

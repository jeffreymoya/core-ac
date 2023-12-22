package com.synpulse8.pulse8.core.accesscontrolsvc.models;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AccountRoles {
    private Account account;
    @Getter
    public static class Account {
        private List<String> roles;
    }
}

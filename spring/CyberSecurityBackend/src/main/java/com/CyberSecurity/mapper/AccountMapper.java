package com.CyberSecurity.mapper;

import com.CyberSecurity.dto.account.AccountDTO;
import com.CyberSecurity.dto.account.GetAccountDetailsDTO;
import com.CyberSecurity.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    @Autowired
    private UserMapper userMapper;

    public GetAccountDetailsDTO toAccountDetailsDTO(Account account) {
        return GetAccountDetailsDTO
                .builder()
                .email(account.getEmail())
                .isUsing2FA(account.isUsing2FA())
                .user(userMapper.toUserDTO(account.getUser()))
                .build();
    }
}

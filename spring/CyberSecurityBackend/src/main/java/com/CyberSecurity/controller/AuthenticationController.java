package com.CyberSecurity.controller;

import com.CyberSecurity.dto.account.AccountDTO;
import com.CyberSecurity.dto.authentication.GetProfileDTO;
import com.CyberSecurity.dto.authentication.RegisterDTO;
import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.model.User;
import com.CyberSecurity.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {
    private final AccountService accountService;

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetProfileDTO> createAccount(@Valid @RequestBody RegisterDTO registrationData){
        User registered = accountService.register(registrationData);
        if (registered == null) return new ResponseEntity<>(HttpStatus.CONFLICT);
        AccountDTO registeredDTO = new AccountDTO(registrationData.getEmail());
        CreatedUserDTO createdUserDTO = new CreatedUserDTO(registered);
        GetProfileDTO profile = new GetProfileDTO(createdUserDTO, registeredDTO);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }
}

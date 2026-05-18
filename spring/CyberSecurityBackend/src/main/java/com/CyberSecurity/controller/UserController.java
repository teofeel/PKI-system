package com.CyberSecurity.controller;

import com.CyberSecurity.dto.account.GetAccountDetailsDTO;
import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.dto.user.UpdateUserDTO;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.User;
import com.CyberSecurity.service.AccountService;
import com.CyberSecurity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private final UserService userService;
    private final AccountService accountService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CreatedUserDTO>> getUsers(){
        try{
            List<CreatedUserDTO> dto = userService.getAllUsers();
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetAccountDetailsDTO> getUser(@AuthenticationPrincipal Account acc){
        try{
            GetAccountDetailsDTO dto = accountService.getAccount(acc.getId());

            return new ResponseEntity<>(dto, HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PutMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserProfile(@AuthenticationPrincipal Account acc, @Valid @RequestBody UpdateUserDTO data){
        try{
            userService.updateUser(acc.getId(), data);

            return new ResponseEntity<>(HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

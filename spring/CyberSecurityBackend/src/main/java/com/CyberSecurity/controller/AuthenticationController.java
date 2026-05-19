package com.CyberSecurity.controller;

import com.CyberSecurity.dto.ChallengeDTO;
import com.CyberSecurity.dto.account.AccountDTO;
import com.CyberSecurity.dto.authentication.*;
import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.exceptions.RefreshTokenException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.User;
import com.CyberSecurity.service.AccountService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.cmp.Challenge;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {
    private final AuthenticationManager authManager;
    private final AccountService accountService;

    @PostMapping(value = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO data, BindingResult result) {
        if (result.hasErrors()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try{
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Account account = (Account) authentication.getPrincipal();
            if (account == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            LoginResponseDTO res = accountService.login(account);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (BadCredentialsException | UsernameNotFoundException | InternalAuthenticationServiceException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch(AccountStatusException e){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO requestDTO){
        try {
            RefreshTokenResponseDTO responseDTO = accountService.refreshToken(requestDTO.getRefreshToken());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }catch (RefreshTokenException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterCAUserDTO dto) {
        User registered = accountService.registerCAUser(dto);
        if (registered == null) return new ResponseEntity<>(HttpStatus.CONFLICT);
        AccountDTO registeredDTO = new AccountDTO(dto.getEmail());
        CreatedUserDTO createdUserDTO = new CreatedUserDTO(registered);
        GetProfileDTO profile = new GetProfileDTO(createdUserDTO, registeredDTO);
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @GetMapping(value = "/verify/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChallengeDTO> verifyAccount(@PathVariable String token){
        Account acc = accountService.verifyAccountToken(token);
        if (acc == null)
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        String secret = accountService.generateVerificationChallenge(acc);
        ChallengeDTO dto = new ChallengeDTO(secret);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/verify/{token}/{decrypted}")
    public ResponseEntity<?> verifyAccount(@PathVariable String token, @PathVariable String decrypted) {

        boolean isCorrect = accountService.verifyDecryptedSecret(token,decrypted);
        if (isCorrect){
            return ResponseEntity.ok(HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Neispravan ključ!");
        }
    }

    @GetMapping(value = "/set-password/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> verifySetPasswordToken(@PathVariable String token){
        boolean checkToken = accountService.verifySetPasswordToken(token);
        if(!checkToken)
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        return ResponseEntity.ok(null);
    }

    @PostMapping(value = "/set-password/{token}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setPassword(@PathVariable String token, @Valid @RequestBody SetPasswordDTO passwordDTO){
        if (accountService.setPassword(token, passwordDTO.getPassword()))
            return ResponseEntity.ok(null);
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);

    }

    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDTO requestDTO){
        try{
            accountService.logout(requestDTO);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

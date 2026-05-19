package com.CyberSecurity.controller;

import com.CyberSecurity.dto.authentication.LoginResponseDTO;
import com.CyberSecurity.dto.mfa.MfaCode;
import com.CyberSecurity.dto.mfa.MfaQrCode;
import com.CyberSecurity.enums.MfaVerifyStatus;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.exceptions.InvalidCodeException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.service.MfaService;
import com.CyberSecurity.utils.JWTService;
import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mfa")
public class MfaController {
    @Autowired
    private MfaService mfaService;
    @Autowired
    private JWTService jwtService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PostMapping("/setup")
    public ResponseEntity<MfaQrCode> setupMfa(@AuthenticationPrincipal Account acc){
        try{
            MfaQrCode dto = mfaService.generateCode(acc.getId());

            return new ResponseEntity<>(dto, HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(QrGenerationException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PostMapping("/setup/verify")
    public ResponseEntity<?> setupMfaVerify(@AuthenticationPrincipal Account acc, @Valid @RequestBody MfaCode code){
        try{
            System.out.println(code.getCode().toString());
            mfaService.verify(acc.getId(), code, MfaVerifyStatus.SETUP_VERIFY);

            return new ResponseEntity<>(HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(InvalidCodeException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PutMapping("/setup/verify")
    public ResponseEntity<?> removeMfaVerify(@AuthenticationPrincipal Account acc, @Valid @RequestBody MfaCode code){
        try{
            System.out.println(code.getCode().toString());
            mfaService.verify(acc.getId(), code, MfaVerifyStatus.REMOVE);

            return new ResponseEntity<>(HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(InvalidCodeException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('TWOFA')")
    @PostMapping("/verify")
    public ResponseEntity<LoginResponseDTO> loginMfaVerify(@AuthenticationPrincipal Account acc, @Valid @RequestBody MfaCode code){
        try{
            mfaService.verify(acc.getId(), code, MfaVerifyStatus.LOGIN_VERIFY);

            String finalToken = jwtService.generateToken(acc);

            LoginResponseDTO dto = LoginResponseDTO.builder()
                    .accessToken(finalToken)
                    .using2FA(false)
                    .build();

            return new ResponseEntity<>(dto, HttpStatus.OK);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch(InvalidCodeException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

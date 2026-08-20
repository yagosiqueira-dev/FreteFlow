package br.com.freteflow.controller;

import br.com.freteflow.dto.auth.LoginDTO;
import br.com.freteflow.dto.auth.LoginResponseDTO;
import br.com.freteflow.dto.auth.RegisterDTO;
import br.com.freteflow.entity.User;
import br.com.freteflow.security.TokenService;
import br.com.freteflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO data) {

        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());


        var auth = this.authenticationManager.authenticate(usernamePassword);


        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterDTO data) {
        userService.createUser(data.name(), data.email(), data.password(), data.role());
        return ResponseEntity.ok().build();
    }
}
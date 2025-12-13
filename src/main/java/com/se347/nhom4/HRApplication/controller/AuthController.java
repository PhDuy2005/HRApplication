package com.se347.nhom4.HRApplication.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqLoginDTO;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResLoginDTO;
import com.se347.nhom4.HRApplication.domain.table.Employee;
import com.se347.nhom4.HRApplication.service.EmployeeService;
import com.se347.nhom4.HRApplication.util.SecurityUtil;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final EmployeeService employeeService;

    @Value("${se347.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    @PostMapping("/login")
    @ApiMessage("Đăng nhập")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
        System.out.println(">>>AUTH MODULE: Login attempt for username: " + loginDTO.getUsername());
        // Implement login logic here
        // Nap input gõm username/password vào SecurityToken
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // Xác thực người dùng => cán việt hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Nap thông tin (nếu xử lý thành công) vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // securityUtil.createAccessToken(authentication);
        ResLoginDTO resLoginDTO = new ResLoginDTO();

        Employee currentUserDB = this.employeeService.handleFindByUsername(loginDTO.getUsername());
        if (currentUserDB == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(currentUserDB.getId());
        userLogin.setEmail(loginDTO.getUsername());
        userLogin.setName(currentUserDB.getFullname());
        userLogin.setRole(currentUserDB.getRole());
        resLoginDTO.setUser(userLogin);
        String accessToken = securityUtil.createAccessToken(loginDTO.getUsername(), resLoginDTO);

        // create refresh_token
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);
        // update user
        this.employeeService.updateUserRefreshToken(refresh_token, loginDTO.getUsername());

        // set cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        System.out.println(">>>AUTH MODULE: Login successful for username: " + loginDTO.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        System.out.println(">>>AUTH MODULE: Fetching account information");

        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";
        Employee currentUserDB = this.employeeService.handleFindByUsername(email);
        if (currentUserDB == null) {
            // return;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userLogin.setId(currentUserDB.getId());
        userLogin.setEmail(email);
        userLogin.setName(currentUserDB.getFullname());
        userLogin.setRole(currentUserDB.getRole());
        userGetAccount.setUser(userLogin);
        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/refresh")
    @ApiMessage("Lấy token mới bằng refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "No cookies") String refreshToken)
            throws BadRequestException {
        System.out.println(">>>AUTH MODULE: Refresh token attempt for token: " + refreshToken);
        if (refreshToken.equals("No cookies")) {
            System.out.println(">>>AUTH MODULE: No refresh token provided");
            throw new BadRequestException("No refresh token provided");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject(); // trường "sub" trong JWT lưu email hoặc cái gì đó unique
        // Tìm user theo email và refresh token
        Employee currentUser = this.employeeService.handleFindByEmailAndRefreshToken(email, refreshToken);
        if (currentUser == null) {
            System.out.println(">>>AUTH MODULE: Invalid refresh token for email: " + email);
            throw new BadRequestException("Invalid refresh token");
        }
        // return ResponseEntity.ok(email);

        ResLoginDTO resLoginDTO = new ResLoginDTO();

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(currentUser.getId());
        userLogin.setEmail(currentUser.getEmail());
        userLogin.setName(currentUser.getFullname());
        userLogin.setRole(currentUser.getRole());
        resLoginDTO.setUser(userLogin);
        String accessToken = securityUtil.createAccessToken(email, resLoginDTO);

        // create refresh_token
        String new_refresh_token = this.securityUtil.createRefreshToken(currentUser.getEmail(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);
        // update user
        this.employeeService.updateUserRefreshToken(new_refresh_token, currentUser.getEmail());

        // set cookies
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        System.out.println(">>>AUTH MODULE: Refresh token successful for email: " + email);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(resLoginDTO);
    }

    @PostMapping("/logout")
    @ApiMessage("Đăng xuất")
    public ResponseEntity<Void> logout() throws BadRequestException {
        System.out.println(">>>AUTH MODULE: Logout attempt");
        String email = this.securityUtil.getCurrentUserLogin().isPresent()
                ? this.securityUtil.getCurrentUserLogin().get()
                : null;
        if (email == null) {
            throw new BadRequestException("No refresh token provided");
        }
        this.employeeService.handleLogOutUser(email);

        // set cookies
        ResponseCookie deleteCookies = ResponseCookie.from("refresh_token", null)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        System.out.println(">>>AUTH MODULE: Logout successful for email: " + email);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookies.toString())
                .build();
    }
}

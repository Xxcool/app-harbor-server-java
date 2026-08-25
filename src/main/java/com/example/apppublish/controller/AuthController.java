package com.example.apppublish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.apppublish.common.ApiResponse;
import com.example.apppublish.common.BusinessException;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.SysUserMapper;
import com.example.apppublish.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final SysUserMapper users; private final PasswordEncoder encoder; private final JwtService jwt; private final JdbcTemplate jdbc;
  public AuthController(SysUserMapper users, PasswordEncoder encoder, JwtService jwt, JdbcTemplate jdbc) { this.users = users; this.encoder = encoder; this.jwt = jwt; this.jdbc = jdbc; }
  @PostMapping("/login") public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) { var user = users.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.username())); if (user == null || !encoder.matches(request.password(), user.getPasswordHash())) throw new BusinessException("4001", "用户名或密码错误"); return ApiResponse.ok(new LoginResult(jwt.issue(user.getId(), user.getUsername()), "", user.getId(), user.getUsername())); }
  @GetMapping("/user-info") public ApiResponse<UserInfo> userInfo(@AuthenticationPrincipal SysUser user) { var roleCodes = jdbc.query("SELECT r.code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.deleted=0", (rs, row) -> rs.getString(1), user.getId()); var roles = roleCodes.stream().map(code -> "SUPER_ADMIN".equals(code) ? "R_SUPER" : "R_" + code).toList(); return ApiResponse.ok(new UserInfo(user.getId(), user.getUsername(), user.getDisplayName(), roles, "R_SUPER".equals(roles.stream().findFirst().orElse("")) ? List.of("*:*:*") : List.of())); }
  public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
  public record LoginResult(String token, String refreshToken, Long userId, String userName) {}
  public record UserInfo(Long userId, String userName, String nickName, List<String> roles, List<String> buttons) {}
}

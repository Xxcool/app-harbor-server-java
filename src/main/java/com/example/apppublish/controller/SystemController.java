package com.example.apppublish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.apppublish.common.ApiResponse;
import com.example.apppublish.common.BusinessException;
import com.example.apppublish.domain.OperationLog;
import com.example.apppublish.domain.SysRole;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.OperationLogMapper;
import com.example.apppublish.mapper.SysRoleMapper;
import com.example.apppublish.mapper.SysUserMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.apppublish.service.AuditLogService;

@RestController @RequestMapping("/api/system") @PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemController {
  private final SysUserMapper users; private final SysRoleMapper roles; private final OperationLogMapper logs; private final PasswordEncoder encoder; private final JdbcTemplate jdbc; private final AuditLogService audit;
  public SystemController(SysUserMapper users, SysRoleMapper roles, OperationLogMapper logs, PasswordEncoder encoder, JdbcTemplate jdbc, AuditLogService audit) { this.users = users; this.roles = roles; this.logs = logs; this.encoder = encoder; this.jdbc = jdbc; this.audit = audit; }
  @GetMapping("/users") public ApiResponse<Page<SysUser>> users(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "20") long size, @RequestParam(required = false) String keyword) { var query = new LambdaQueryWrapper<SysUser>().select(SysUser.class, f -> !"password_hash".equals(f.getColumn())).orderByDesc(SysUser::getCreatedAt); if (keyword != null && !keyword.isBlank()) query.and(q -> q.like(SysUser::getUsername, keyword).or().like(SysUser::getDisplayName, keyword)); return ApiResponse.ok(users.selectPage(Page.of(page, size), query)); }
  @PostMapping("/users") public ApiResponse<Void> createUser(@Valid @RequestBody UserRequest request) { if (users.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.username())) > 0) throw new BusinessException("4201", "用户名已存在"); var user = new SysUser(); user.setUsername(request.username()); user.setDisplayName(request.displayName()); user.setEmail(request.email()); user.setPasswordHash(encoder.encode(request.password())); user.setStatus("ENABLED"); users.insert(user); if (request.roleCodes() != null) request.roleCodes().forEach(code -> jdbc.update("INSERT IGNORE INTO sys_user_role(user_id, role_id) SELECT ?, id FROM sys_role WHERE code=? AND deleted=0", user.getId(), code)); audit.record("CREATE_USER", "SYS_USER", user.getId(), user.getUsername()); return ApiResponse.ok(); }
  @PutMapping("/users/{id}/status") public ApiResponse<Void> userStatus(@PathVariable Long id, @RequestBody StatusRequest request) { var user = users.selectById(id); if (user == null) throw new BusinessException("4042", "用户不存在"); user.setStatus(request.status()); users.updateById(user); audit.record("CHANGE_USER_STATUS", "SYS_USER", id, request.status()); return ApiResponse.ok(); }
  @GetMapping("/roles") public ApiResponse<java.util.List<SysRole>> roles() { return ApiResponse.ok(roles.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId))); }
  @PostMapping("/roles") public ApiResponse<Void> createRole(@RequestBody SysRole role) { roles.insert(role); audit.record("CREATE_ROLE", "SYS_ROLE", role.getId(), role.getCode()); return ApiResponse.ok(); }
  @GetMapping("/logs") public ApiResponse<Page<OperationLog>> logs(@RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "20") long size) { return ApiResponse.ok(logs.selectPage(Page.of(page, size), new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreatedAt))); }
  public record UserRequest(@NotBlank String username, @NotBlank String displayName, String email, @NotBlank String password, java.util.List<String> roleCodes) {}
  public record StatusRequest(String status) {}
}

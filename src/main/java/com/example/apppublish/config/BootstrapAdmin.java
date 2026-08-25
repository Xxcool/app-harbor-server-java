package com.example.apppublish.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

/** 首次启动时创建开发管理员，生产环境应立即修改密码。 */
@Component
public class BootstrapAdmin implements CommandLineRunner {
  private final SysUserMapper users; private final PasswordEncoder encoder; private final JdbcTemplate jdbc; private final AppProperties properties;
  public BootstrapAdmin(SysUserMapper users, PasswordEncoder encoder, JdbcTemplate jdbc, AppProperties properties) { this.users = users; this.encoder = encoder; this.jdbc = jdbc; this.properties = properties; }
  @Override public void run(String... args) { var user = users.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")); if (user == null) { if (properties.bootstrapAdminPassword() == null || properties.bootstrapAdminPassword().length() < 12) throw new IllegalStateException("INITIAL_ADMIN_PASSWORD 必须设置且至少 12 位"); user = new SysUser(); user.setUsername("admin"); user.setDisplayName("平台管理员"); user.setPasswordHash(encoder.encode(properties.bootstrapAdminPassword())); user.setStatus("ENABLED"); users.insert(user); } jdbc.update("INSERT IGNORE INTO sys_user_role(user_id, role_id) SELECT ?, id FROM sys_role WHERE code='SUPER_ADMIN'", user.getId()); }
}

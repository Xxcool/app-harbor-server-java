package com.example.apppublish.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.SysUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService; private final SysUserMapper userMapper; private final JdbcTemplate jdbc;
  public JwtAuthenticationFilter(JwtService jwtService, SysUserMapper userMapper, JdbcTemplate jdbc) { this.jwtService = jwtService; this.userMapper = userMapper; this.jdbc = jdbc; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    var header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
      try { var username = jwtService.subject(header.substring(7)); SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)); if (user != null && "ENABLED".equals(user.getStatus())) { var authorities = jdbc.query("SELECT r.code FROM sys_role r JOIN sys_user_role ur ON ur.role_id=r.id WHERE ur.user_id=? AND r.deleted=0", (rs, row) -> new SimpleGrantedAuthority("ROLE_" + rs.getString(1)), user.getId()); SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities)); } } catch (Exception ignored) { }
    }
    chain.doFilter(request, response);
  }
}

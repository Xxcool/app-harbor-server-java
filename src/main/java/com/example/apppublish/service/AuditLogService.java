package com.example.apppublish.service;

import com.example.apppublish.domain.OperationLog;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 统一记录关键发布和系统管理操作。 */
@Service
public class AuditLogService {
  private final OperationLogMapper logs;
  public AuditLogService(OperationLogMapper logs) { this.logs = logs; }
  public void record(String action, String resourceType, Object resourceId, String detail) { var auth = SecurityContextHolder.getContext().getAuthentication(); var log = new OperationLog(); if (auth != null && auth.getPrincipal() instanceof SysUser user) { log.setUserId(user.getId()); log.setUsername(user.getUsername()); } log.setAction(action); log.setResourceType(resourceType); log.setResourceId(resourceId == null ? null : resourceId.toString()); log.setDetail(detail); var attrs = RequestContextHolder.getRequestAttributes(); if (attrs instanceof ServletRequestAttributes servlet) { HttpServletRequest request = servlet.getRequest(); log.setIp(request.getRemoteAddr()); } logs.insert(log); }
}

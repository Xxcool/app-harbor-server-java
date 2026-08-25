package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("sys_user")
public class SysUser extends BaseEntity { private String username; private String passwordHash; private String displayName; private String email; private String status; }

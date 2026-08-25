package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("sys_role")
public class SysRole extends BaseEntity { private String code; private String name; private String description; }

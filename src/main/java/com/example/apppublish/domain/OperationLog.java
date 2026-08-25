package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("operation_log")
public class OperationLog { private Long id; private Long userId; private String username; private String action; private String resourceType; private String resourceId; private String detail; private String ip; private LocalDateTime createdAt; }

package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity { private Long id; private LocalDateTime createdAt; private LocalDateTime updatedAt; @TableLogic private Integer deleted; }

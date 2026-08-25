package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("android_app")
public class AndroidApp extends BaseEntity { private String packageName; private String appName; private String iconObjectKey; private String description; private Long latestReleaseId; private String status; }

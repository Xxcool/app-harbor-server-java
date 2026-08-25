package com.example.apppublish.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true) @TableName("android_release")
public class AndroidRelease extends BaseEntity { private Long appId; private Long versionCode; private String versionName; private Integer buildNo; private String originalFilename; private String objectKey; private Long fileSize; private String sha256; private String signatureSha256; private String minSdk; private String targetSdk; private String releaseNotes; private String updateMode; private String status; private Long downloadCount; private Long uploadedBy; }

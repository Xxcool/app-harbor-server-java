package com.example.apppublish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.apppublish.common.BusinessException;
import com.example.apppublish.domain.AndroidApp;
import com.example.apppublish.domain.AndroidRelease;
import com.example.apppublish.domain.SysUser;
import com.example.apppublish.mapper.AndroidAppMapper;
import com.example.apppublish.mapper.AndroidReleaseMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ReleaseService {
  private final AndroidAppMapper apps; private final AndroidReleaseMapper releases; private final ApkInspector inspector; private final ObjectStorageService storage; private final AuditLogService audit;
  public ReleaseService(AndroidAppMapper apps, AndroidReleaseMapper releases, ApkInspector inspector, ObjectStorageService storage, AuditLogService audit) { this.apps = apps; this.releases = releases; this.inspector = inspector; this.storage = storage; this.audit = audit; }
  @Transactional public AndroidRelease upload(MultipartFile file, String notes, String mode) throws Exception {
    if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".apk")) throw new BusinessException("4100", "请选择有效的 APK 文件");
    var temp = Files.createTempFile("apk-upload-", ".apk");
    try { file.transferTo(temp); String sha256 = digest(temp); if (releases.selectCount(new LambdaQueryWrapper<AndroidRelease>().eq(AndroidRelease::getSha256, sha256)) > 0) throw new BusinessException("4101", "该 APK 已上传过"); var metadata = inspector.inspect(temp); var app = apps.selectOne(new LambdaQueryWrapper<AndroidApp>().eq(AndroidApp::getPackageName, metadata.packageName())); if (app == null) { app = new AndroidApp(); app.setPackageName(metadata.packageName()); app.setAppName(metadata.appName()); app.setStatus("ENABLED"); apps.insert(app); } else { AndroidRelease previous = releases.selectOne(new LambdaQueryWrapper<AndroidRelease>().eq(AndroidRelease::getAppId, app.getId()).isNotNull(AndroidRelease::getSignatureSha256).orderByDesc(AndroidRelease::getCreatedAt).last("LIMIT 1")); if (previous != null && metadata.signatureSha256() != null && !metadata.signatureSha256().equalsIgnoreCase(previous.getSignatureSha256())) throw new BusinessException("4104", "APK 签名与该应用历史版本不一致"); }
      int buildNo = Math.toIntExact(releases.selectCount(new LambdaQueryWrapper<AndroidRelease>().eq(AndroidRelease::getAppId, app.getId()).eq(AndroidRelease::getVersionCode, metadata.versionCode())) + 1); String key = "apps/%s/%s/%s-%s.apk".formatted(metadata.packageName(), metadata.versionCode(), UUID.randomUUID(), safeName(file.getOriginalFilename())); try (InputStream in = Files.newInputStream(temp)) { storage.put(key, in, Files.size(temp), "application/vnd.android.package-archive"); }
      var user = (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); var release = new AndroidRelease(); release.setAppId(app.getId()); release.setVersionCode(metadata.versionCode()); release.setVersionName(metadata.versionName()); release.setBuildNo(buildNo); release.setOriginalFilename(file.getOriginalFilename()); release.setObjectKey(key); release.setFileSize(Files.size(temp)); release.setSha256(sha256); release.setSignatureSha256(metadata.signatureSha256()); release.setMinSdk(metadata.minSdk()); release.setTargetSdk(metadata.targetSdk()); release.setReleaseNotes(notes); release.setUpdateMode(mode == null ? "NORMAL" : mode); release.setStatus("PUBLISHED"); release.setDownloadCount(0L); release.setUploadedBy(user.getId()); releases.insert(release); app.setLatestReleaseId(release.getId()); apps.updateById(app); audit.record("UPLOAD_APK", "ANDROID_RELEASE", release.getId(), metadata.packageName() + "@" + metadata.versionName()); return release;
    } finally { Files.deleteIfExists(temp); }
  }
  private String digest(java.nio.file.Path path) throws Exception { MessageDigest digest = MessageDigest.getInstance("SHA-256"); try (var input = new DigestInputStream(Files.newInputStream(path), digest)) { input.transferTo(OutputStream.nullOutputStream()); } return HexFormat.of().formatHex(digest.digest()); }
  private String safeName(String name) { return name.replaceAll("[^a-zA-Z0-9._-]", "_"); }
}

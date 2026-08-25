package com.example.apppublish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.apppublish.common.ApiResponse;
import com.example.apppublish.common.BusinessException;
import com.example.apppublish.config.AppProperties;
import com.example.apppublish.domain.AndroidApp;
import com.example.apppublish.domain.AndroidRelease;
import com.example.apppublish.mapper.AndroidAppMapper;
import com.example.apppublish.mapper.AndroidReleaseMapper;
import com.example.apppublish.service.ObjectStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class DownloadController {
  private final AndroidAppMapper apps; private final AndroidReleaseMapper releases; private final ObjectStorageService storage; private final AppProperties properties;
  public DownloadController(AndroidAppMapper apps, AndroidReleaseMapper releases, ObjectStorageService storage, AppProperties properties) { this.apps = apps; this.releases = releases; this.storage = storage; this.properties = properties; }
  @GetMapping("/download/releases/{id}") public void download(@PathVariable Long id, HttpServletResponse response, HttpServletRequest request) throws Exception { var release = releases.selectById(id); if (release == null || !"PUBLISHED".equals(release.getStatus())) throw new BusinessException("4041", "版本不可下载"); release.setDownloadCount(release.getDownloadCount() + 1); releases.updateById(release); response.setContentType("application/vnd.android.package-archive"); response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(release.getOriginalFilename(), StandardCharsets.UTF_8)); response.setContentLengthLong(release.getFileSize()); try (var in = storage.get(release.getObjectKey())) { in.transferTo(response.getOutputStream()); } }
  @GetMapping("/download/apps/{packageName}/latest") public void latest(@PathVariable String packageName, HttpServletResponse response, HttpServletRequest request) throws Exception { AndroidApp app = apps.selectOne(new LambdaQueryWrapper<AndroidApp>().eq(AndroidApp::getPackageName, packageName)); if (app == null || app.getLatestReleaseId() == null) throw new BusinessException("4040", "应用暂无可用版本"); download(app.getLatestReleaseId(), response, request); }
  @GetMapping("/api/public/check-update/{packageName}/{currentVersionCode}") public ApiResponse<UpdateInfo> checkUpdate(@PathVariable String packageName, @PathVariable long currentVersionCode) { AndroidApp app = apps.selectOne(new LambdaQueryWrapper<AndroidApp>().eq(AndroidApp::getPackageName, packageName)); if (app == null || app.getLatestReleaseId() == null) return ApiResponse.ok(new UpdateInfo(false, null, null, null, null, null)); AndroidRelease release = releases.selectById(app.getLatestReleaseId()); boolean available = release != null && "PUBLISHED".equals(release.getStatus()) && release.getVersionCode() > currentVersionCode; return ApiResponse.ok(new UpdateInfo(available, release.getVersionCode(), release.getVersionName(), release.getUpdateMode(), release.getReleaseNotes(), properties.publicBaseUrl() + "/download/releases/" + release.getId())); }
  public record UpdateInfo(boolean updateAvailable, Long versionCode, String versionName, String updateMode, String releaseNotes, String downloadUrl) {}
}

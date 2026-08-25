package com.example.apppublish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.apppublish.common.ApiResponse;
import com.example.apppublish.common.BusinessException;
import com.example.apppublish.domain.AndroidApp;
import com.example.apppublish.domain.AndroidRelease;
import com.example.apppublish.mapper.AndroidAppMapper;
import com.example.apppublish.mapper.AndroidReleaseMapper;
import com.example.apppublish.service.ReleaseService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/apps")
public class AppController {
  private final AndroidAppMapper apps; private final AndroidReleaseMapper releases; private final ReleaseService releaseService;
  public AppController(AndroidAppMapper apps, AndroidReleaseMapper releases, ReleaseService releaseService) { this.apps = apps; this.releases = releases; this.releaseService = releaseService; }
  @GetMapping public ApiResponse<Page<AndroidApp>> list(@RequestParam(defaultValue = "1") @Min(1) long page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size, @RequestParam(required = false) String keyword) { var query = new LambdaQueryWrapper<AndroidApp>().orderByDesc(AndroidApp::getUpdatedAt); if (keyword != null && !keyword.isBlank()) query.and(q -> q.like(AndroidApp::getAppName, keyword).or().like(AndroidApp::getPackageName, keyword)); return ApiResponse.ok(apps.selectPage(Page.of(page, size), query)); }
  @GetMapping("/{id}") public ApiResponse<AndroidApp> detail(@PathVariable Long id) { var app = apps.selectById(id); if (app == null) throw new BusinessException("4040", "应用不存在"); return ApiResponse.ok(app); }
  @GetMapping("/{id}/releases") public ApiResponse<Page<AndroidRelease>> versions(@PathVariable Long id, @RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "20") long size) { return ApiResponse.ok(releases.selectPage(Page.of(page, size), new LambdaQueryWrapper<AndroidRelease>().eq(AndroidRelease::getAppId, id).orderByDesc(AndroidRelease::getVersionCode).orderByDesc(AndroidRelease::getBuildNo))); }
  @PostMapping("/upload") public ApiResponse<AndroidRelease> upload(@RequestPart("file") MultipartFile file, @RequestParam(required = false) String releaseNotes, @RequestParam(defaultValue = "NORMAL") String updateMode) throws Exception { return ApiResponse.ok(releaseService.upload(file, releaseNotes, updateMode)); }
  @PutMapping("/{id}/status") public ApiResponse<Void> appStatus(@PathVariable Long id, @RequestBody StatusRequest request) { var app = apps.selectById(id); if (app == null) throw new BusinessException("4040", "应用不存在"); app.setStatus(request.status()); apps.updateById(app); return ApiResponse.ok(); }
  @PutMapping("/{appId}/releases/{id}") public ApiResponse<Void> updateRelease(@PathVariable Long appId, @PathVariable Long id, @RequestBody ReleaseUpdateRequest request) { var release = releases.selectById(id); if (release == null || !appId.equals(release.getAppId())) throw new BusinessException("4041", "版本不存在"); release.setReleaseNotes(request.releaseNotes()); release.setUpdateMode(request.updateMode()); release.setStatus(request.status()); releases.updateById(release); return ApiResponse.ok(); }
  public record StatusRequest(String status) {}
  public record ReleaseUpdateRequest(String releaseNotes, String updateMode, String status) {}
}

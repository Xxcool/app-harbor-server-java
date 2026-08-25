package com.example.apppublish.controller;

import com.example.apppublish.common.ApiResponse;
import com.example.apppublish.domain.AndroidApp;
import com.example.apppublish.domain.AndroidRelease;
import com.example.apppublish.mapper.AndroidAppMapper;
import com.example.apppublish.mapper.AndroidReleaseMapper;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
  private final AndroidAppMapper apps; private final AndroidReleaseMapper releases;
  public DashboardController(AndroidAppMapper apps, AndroidReleaseMapper releases) { this.apps = apps; this.releases = releases; }
  @GetMapping("/summary") public ApiResponse<Summary> summary() { long downloads = releases.selectList(null).stream().mapToLong(r -> r.getDownloadCount() == null ? 0 : r.getDownloadCount()).sum(); long bytes = releases.selectList(null).stream().mapToLong(AndroidRelease::getFileSize).sum(); return ApiResponse.ok(new Summary(apps.selectCount(null), releases.selectCount(null), downloads, bytes)); }
  public record Summary(long appCount, long releaseCount, long downloadCount, long storageBytes) {}
}

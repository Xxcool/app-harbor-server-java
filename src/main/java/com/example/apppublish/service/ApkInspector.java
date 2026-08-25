package com.example.apppublish.service;

import com.example.apppublish.common.BusinessException;
import com.example.apppublish.config.AppProperties;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 调用 Android 官方工具解析 APK 元数据与签名证书。 */
@Component
public class ApkInspector {
  private static final Pattern PACKAGE = Pattern.compile("package: name='([^']+)' versionCode='([^']+)' versionName='([^']*)'");
  private static final Pattern SDK = Pattern.compile("sdkVersion:'([^']+)'"), TARGET = Pattern.compile("targetSdkVersion:'([^']+)'");
  private static final Pattern LABEL = Pattern.compile("application-label:'([^']*)'");
  private static final Pattern SIGNATURE = Pattern.compile("Signer #1 certificate SHA-256 digest: ([A-Fa-f0-9:]+)");
  private final AppProperties properties;
  public ApkInspector(AppProperties properties) { this.properties = properties; }
  public ApkMetadata inspect(Path apk) { String badging = run(List.of(properties.apk().aapt2Path(), "dump", "badging", apk.toString())); Matcher pkg = PACKAGE.matcher(badging); if (!pkg.find()) throw new BusinessException("4102", "无法解析 APK 包信息"); String signature = run(List.of(properties.apk().apksignerPath(), "verify", "--print-certs", apk.toString())); return new ApkMetadata(pkg.group(1), Long.parseLong(pkg.group(2)), pkg.group(3), find(LABEL, badging, pkg.group(1)), find(SDK, badging, null), find(TARGET, badging, null), find(SIGNATURE, signature, null)); }
  private String find(Pattern pattern, String text, String fallback) { Matcher matcher = pattern.matcher(text); return matcher.find() ? matcher.group(1) : fallback; }
  private String run(List<String> command) { try { Process process = new ProcessBuilder(new ArrayList<>(command)).redirectErrorStream(true).start(); String output = new String(process.getInputStream().readAllBytes()); if (process.waitFor() != 0) throw new BusinessException("4103", "APK 工具执行失败：" + output.strip()); return output; } catch (BusinessException e) { throw e; } catch (Exception e) { throw new BusinessException("4103", "APK 解析工具不可用"); } }
  public record ApkMetadata(String packageName, long versionCode, String versionName, String appName, String minSdk, String targetSdk, String signatureSha256) {}
}

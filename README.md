# 安卓应用分发平台 · Java 后端

基于 Spring Boot 3、Java 21、MySQL 和 MinIO 实现的安卓 APK 上传、版本管理与下载服务。

> 本仓库是适用于自建服务器的 Java 版本。当前 `app-harbor-43g.pages.dev` 线上环境使用的是独立的 Cloudflare Worker 后端：[Xxcool/app-harbor-api](https://github.com/Xxcool/app-harbor-api)。

## 功能

- JWT 登录认证与基于角色的接口权限
- 用户、角色与操作日志
- APK 上传及清单解析
- 根据安卓包名自动创建或匹配应用
- 版本号、SDK、SHA-256 和签名摘要记录
- MinIO 对象存储及公开下载
- MySQL + Flyway 数据库迁移
- 仪表盘统计和客户端更新检查

## 技术栈

- Java 21
- Spring Boot 3.5
- Spring Security
- MyBatis Plus
- MySQL 8
- Flyway
- MinIO
- `aapt2` 与 `apksigner`

## 环境变量

| 名称 | 用途 |
| --- | --- |
| `DB_URL` | MySQL JDBC 地址 |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | JWT 签名密钥，建议至少 32 字节 |
| `INITIAL_ADMIN_PASSWORD` | 初始 `admin` 密码，至少 12 位 |
| `MINIO_ENDPOINT` | MinIO 服务地址 |
| `MINIO_ACCESS_KEY` | MinIO Access Key |
| `MINIO_SECRET_KEY` | MinIO Secret Key |
| `MINIO_BUCKET` | APK Bucket，默认 `apk-releases` |
| `PUBLIC_BASE_URL` | 对外访问本服务的基础地址 |
| `AAPT2_PATH` | Android SDK `aapt2` 路径 |
| `APKSIGNER_PATH` | Android SDK `apksigner` 路径 |

敏感配置不得写入 Git，请使用环境变量或部署平台的 Secret 管理功能。

## 本地启动

先启动 MySQL 和 MinIO，并安装 Android SDK Build Tools，然后执行：

```bash
mvn spring-boot:run
```

默认监听端口及完整配置见 `src/main/resources/application.yml`。

## 构建与测试

```bash
mvn test
mvn clean package
```

构建容器镜像：

```bash
docker build -t app-harbor-server-java .
```

## API 约定

业务接口统一返回：

```json
{
  "code": "0000",
  "msg": "success",
  "data": {}
}
```

主要接口：

- `POST /api/auth/login`
- `POST /api/apps/upload`
- `GET /api/apps`
- `GET /api/apps/{id}/releases`
- `GET /api/public/check-update/{packageName}/{versionCode}`
- `GET /download/apps/{packageName}/latest`

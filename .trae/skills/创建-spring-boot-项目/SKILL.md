---
name: 创建 Spring Boot 项目
description: 当用户说：“创建一个 Spring Boot 项目”的时候，在 Trae 当前工作区中创建一个完整的 Spring Boot Web 项目，包含标准目录结构、Maven 构建配置、一个演示用的 REST API 接口。用户可通过一条命令启动应用并立即看到运行结果。
---

# Trae 技能：创建 Spring Boot 项目

## 技能描述
在 Trae 当前工作区中创建一个完整的 Spring Boot Web 项目，包含标准目录结构、Maven 构建配置、一个演示用的 REST API 接口。用户可通过一条命令启动应用并立即看到运行结果。

## 适用场景
- 快速搭建 Spring Boot 原型项目
- 学习或测试 Spring Boot 基础功能
- 作为微服务、Web 应用开发的基础骨架

## 前置条件
- Trae 工作区已打开（任意文件夹）
- 系统已安装 JDK 17 或更高版本
- 系统已安装 Maven（或使用 Trae 内置的 Maven 工具）
- 网络正常（用于首次下载 Spring Boot 依赖）

## 技能执行步骤
当用户触发该技能（例如说“创建一个 Spring Boot 项目”）时，AI 将依次在项目根目录下创建以下文件及文件夹：
project-root/
├── pom.xml
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/
│ │ │ └── example/
│ │ │ └── demo/
│ │ │ ├── DemoApplication.java
│ │ │ └── controller/
│ │ │ └── HelloController.java
│ │ └── resources/
│ │ └── application.properties

## 常见问题与解决方案

### 1. Maven 依赖下载失败（网络问题）

**问题表现：**
```
Non-resolvable parent POM for com.example:demo:1.0.0: 
Could not transfer artifact org.springframework.boot:spring-boot-starter-parent:pom:3.2.0 
from/to aliyun-repo: 不知道这样的主机。(maven.aliyun.com)
```

**解决方案：**

**方案一：检查 Maven 镜像配置**

确保你的 `settings.xml`（通常位于 `~/.m2/settings.xml` 或 `{maven_home}/conf/settings.xml`）正确配置了镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyun-repo</id>
    <mirrorOf>*</mirrorOf>
    <name>Aliyun Maven Repository</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

**方案二：使用本地已有的依赖**

如果网络不可用，可以修改 `pom.xml` 使用本地仓库中已有的 Spring Boot 版本：

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.2.0</version>  <!-- 使用本地已有的版本 -->
  <relativePath/>
</parent>
```

**方案三：检查本地仓库配置**

确保 `settings.xml` 中正确配置了本地仓库路径：

```xml
<localRepository>C:\path\to\your\mavenRepository</localRepository>
```

### 2. 端口被占用

**问题表现：**
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案：**

**方案一：停止占用端口的进程（Windows）**
```cmd
netstat -ano | findstr ":8080"
taskkill /F /PID <进程ID>
```

**方案二：修改应用端口**

在 `application.properties` 中修改端口：
```properties
server.port=8081
```

### 3. Java 版本不兼容

**问题表现：**
- 编译错误提示 Java 版本不兼容
- `java.lang.UnsupportedClassVersionError`

**解决方案：**

确保使用 Java 17 或更高版本，并在 `pom.xml` 中正确配置：

```xml
<properties>
  <java.version>17</java.version>
</properties>
```

## 启动方式

```bash
# 进入项目目录
cd project-root

# 运行应用
mvn spring-boot:run
```

启动成功后访问 `http://localhost:8080` 即可看到效果。
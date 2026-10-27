# sail-framework 技术栈补齐重构计划（修订版：现有依赖版本一律不动）

## 范围界定

- **版本零变更**：mybatis-plus 3.5.17、redisson 4.7.0（含 redisson-spring-data-35 方案）、awssdk 2.55.5、lombok 1.18.48、mapstruct 1.7.0.Beta2、guava 33.7.1-jre、druid、lock4j 2.2.7、springdoc 2.8.17、SBA 3.5.9、rocketmq 2.3.6、hutool 5.8.47 等全部保持现状；MySQL 驱动已由 Boot BOM 管理，核验即可。
- **只补缺失能力**：① Scalar 文档 UI（替换 knife4j，已确认）；② Micrometer Tracing Brave Bridge（替换无 SDK 的 OTel 空转代码，已确认）；③ 把目前因 `optional` 声明而**运行时实际未生效**的 Actuator/Prometheus/SBA Client/SBA Server 接通。
- **核验保持**：Redis Stream（starter-mq 已有消费组/pending 重发）、Quartz JDBC 集群（dev 配置+quartz.sql）、RabbitMQ/RocketMQ optional 默认不启用、OAuth2 Token+BCrypt、多租户/数据权限——均不动。
- 腾讯云 SDK：按你的决定不引入。

## Phase 0：回滚基线

当前目录无 Git 仓库。`git init` + 初始 commit（.gitignore 已忽略 target/.flattened-pom.xml），跑 `mvn -DskipTests clean package` 确认基线可构建。每个 Phase 单独 commit。

## Phase 1：API 文档 knife4j → Scalar

- BOM：删除 knife4j 4.5.0 管理；新增 `com.scalar.maven:scalar-webmvc` 0.6.67（新构件，联网已核验存在；覆盖 springdoc 2.8.17 默认 pin 的 0.5.55）。
- `starter-web/pom.xml`：删 knife4j starter 与 `springdoc-openapi-starter-webmvc-ui`，新增 `springdoc-openapi-starter-webmvc-scalar`（2.8.17，springdoc 官方 Scalar starter）。
- 删除 `Knife4jOpenApiCustomizer.java`；重写 `Sail-frameworkSwaggerAutoConfiguration` 为纯 springdoc 实现，保留 admin-api/app-api 分组与 tenant-id/Authorization 全局 header 定制。
- `application.yaml`：移除 `knife4j.enable`，加 scalar-ui 路径配置；SystemWebConfiguration/InfraWebConfiguration 的 security 放行清单补 scalar-ui/api-docs 路径。
- 兼容性核验：javap 比对 springdoc scalar starter 所引用 scalar-webmvc 类签名（0.5.55 vs 0.6.67）；不兼容则回退为直接集成 scalar-webmvc 0.6.67 自带的 Spring 配置（绕过 springdoc starter）。

## Phase 2：链路追踪 Brave + 监控生效

- `sail-framework-common`：`TracerUtils` 重写为读 MDC `traceId`（Brave 桥经 Boot 自动写入 MDC），`getTraceId()` 签名与空值语义不变，common 移除 opentelemetry-api 依赖且不新增依赖。
- `starter-monitor` pom：删 OTel 三件套；新增 `micrometer-tracing-bridge-brave` 与 `spring-boot-starter-actuator`（生效）；`micrometer-registry-prometheus`、`spring-boot-admin-starter-client` 由 optional→生效；保留 skywalking apm-toolkit-logback（optional）。
- 重写 monitor 的 `Sail-frameworkTracerAutoConfiguration`（@ConditionalOnClass io.micrometer.tracing.Tracer，消费 Boot BraveAutoConfiguration 提供的 Tracer Bean）、`BizTraceAspect`、`TracerFrameworkUtils` 为 Micrometer Tracing API。
- BOM：删除 opentelemetry 三件套管理（改造后零引用）；skywalking 保留。
- `module-infra`：`spring-boot-admin-starter-server` optional→生效（确认已有 AdminServerConfiguration）；security 放行 `/admin/**`。
- `application-local/dev.yaml`：加 `management.tracing.sampling.probability: 1.0`；prometheus 端点已被 expose '*' 覆盖，核验即可。

## Phase 3：测试与全量验证

- 新增无外部依赖纯单元测试：`TimeoutRedisCacheManager` TTL 解析（starter-redis）、`TracerUtils`（common）、`CronUtils`（starter-job），让 `mvn test` 真实跑起来且全绿；不改任何现有断言。
- `mvn clean package` 全仓库通过；`dependency:tree` 核验 scalar-webmvc 0.6.67、micrometer-tracing-bridge-brave 在列且 OTel 消失。
- 运行时烟测（视本机 MySQL/Redis 而定）：/v3/api-docs、scalar UI、/actuator/prometheus、trace-id 响应头；环境不具备则明确记录为待办。

## Phase 4：文档与 CI 收尾

- README 技术栈表改为与实际一致（现有版本保持后的真实版本号 + Scalar + Micrometer Tracing Brave）。
- `.github/workflows/maven.yml` JDK matrix 8/11/17 → 17（Java 17 是目标栈要求，现状 CI 必失败）。
- 部署文件（Dockerfile/deploy.sh/Jenkinsfile/docker-compose）不变，健康检查端点不变。

## 风险与说明

- scalar-webmvc 0.6.67 强制覆盖 springdoc pin 的 0.5.55 存在二进制兼容风险，已备回退方案（Phase 1 内核验）。
- SBA server/client 与 Actuator 激活属于运行时行为变化（从"配置了但不生效"变为真生效），安全放行随 Phase 1/2 同步处理，不关闭任何现有安全机制。
- 不修改与需求无关的代码：AI/iot/trade/flowable/weixin/justauth 等配置段残留、`sail-framework.tencent-lbs-key` 残留均不动（仅报告提示）。
- local profile 对 QuartzAutoConfiguration 的排除保留（本地无 QRTZ_ 表的启动保护）。

## 验证命令

`mvn clean package`（逐 Phase）、`mvn dependency:tree -Dincludes=io.micrometer,com.scalar.maven,org.springdoc,io.opentelemetry`、分阶段 `git log` 可回滚。
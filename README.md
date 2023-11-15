# 项目名称

OL 授课中心

# 目录

* [项目介绍](#项目介绍)

* [项目架构](#项目架构)
* [技术选型](#技术选型)
* [项目部署](#项目部署)

# 项目介绍

OL 授课中心以教育授课为主题（包括用户端、机构端、运营端），基于Spring Cloud 微服务架构进行构建，使用 SpringBoot、Spring Cloud、MyBatis-Plus、MQ、Redis、Elasticsearch 等框架和中间件完成开发，其中项目的功能模块主要有内容管理、媒资管理、课程搜索、选课管理和认证授权。

为保证项目的稳定性，开发者将项目划分为内容管理服务、媒资管理服务、搜索服务、订单支付服务、学习中心服务、系统管理服务、认证授权服务、网关服务、注册中心服务和配置中心服务。

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-223.png)

# 项目架构

> 技术架构

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-04.png)

> 业务流程

**课程编辑与发布流程** 

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-02.png)

备注：课程发布后学生登录平台进行选课、在线学习, 免费课程可直接学习，收费课程需要下单购买

**学生选课流程**   

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-03.png)

# 技术选型


- Spring Cloud + Spring Cloud Alibaba 微服务
  - Nacos 注册中心和配置中心
  - Ribbon 负载均衡
  - Feign 客户端调用
  - Gateway 网关
- Spring Boot
- MySQL 
- MyBatis-Plus
- Redis
- RabbitMQ 
- MinIO
- Elasticsearch 
- XXL-JOB
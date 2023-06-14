# 配置1

> online-education-content-api-dev.yaml 
>
> data_id: online-education-content-api-dev.yaml
>
> group: online-education-backend

```yml
server:
  servlet:
    context-path: /content
  port: 63040
```

# 配置2

>  online-education-content-service-dev.yaml 
>
> data_id: online-education-content-service-dev.yaml
>
> group: online-education-backend

```yml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/online_education_content?userUnicode=true&useSSL=false
    username: root
    password: root
```

# 配置3 

> swagger-dev.yaml
>
> data_id：swagger-dev.yaml
>
> group: online-education-common

```yml
swagger:
  title: "在线教育网站"
  description: "内容系统管理系统对课程相关信息进行业务管理数据"
  base-package: com.cyan.springcloud.content
  enabled: true
  version: 1.0.0
```

# 配置4

> logging-dev.yaml
>
> data_id: logging-dev.yaml
>
> group: online-education-common

```yml
logging:
  config: classpath:log4j2-dev.xml
```


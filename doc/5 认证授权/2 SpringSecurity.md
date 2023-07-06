# 介绍

Spring Security 是一个功能强大且高度可定制的身份验证和访问控制框架，它是一个专注于为 Java 应用程序提供身份验证和授权的框架。  

由于本项目基于Spring Cloud技术构建，Spring Security是spring家族的一份子且和Spring Cloud集成的很好，所以本项目选用Spring Security作为认证服务的技术框架。  

项目主页：https://spring.io/projects/spring-security

Spring cloud Security： https://spring.io/projects/spring-cloud-security

# 认证授权入门

## 创建认证服务

**1.部署认证服务**

**2.创建用户数据库**

**3.在nacos新增auth-service-dev.yaml  **

**4.启动工程，尝试访问**http://localhost:63070/auth/r/r1 :

## 认证测试

1.向auth认证工程集成Spring security，向pom.xml加入Spring Security所需要的依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
```

重启工程，访问http://localhost:63070/auth/r/r1

2.安全配置

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
}
```

用户信息  

```java
// 配置用户信息服务
@Bean
public UserDetailsService userDetailsService() {
    //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
    manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
    return manager;
}
```

密码方式

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // 密码为明文方式
    return NoOpPasswordEncoder.getInstance();
//        return new BCryptPasswordEncoder();
}
```

安全拦截机制

```java
// 配置安全拦截机制
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
            .authorizeRequests()
            .antMatchers("/r/**").authenticated() // 访问/r开始的请求需要认证通过
            .anyRequest().permitAll() // 其它请求全部放行
            .and()
            .formLogin().successForwardUrl("/login-success"); // 登录成功跳转到/login-success
}
```

重启工程

1、访问http://localhost:63070/auth/user/52 可以正常访问

2、访问http://localhost:63070/auth/r/r1 显示登录页面

## 授权测试

用户认证通过去访问系统资源时spring security进行授权控制，判断用户是否有该资源的访问权限，如果有则继续访问，如果没有则拒绝访问。

配置用户拥有哪些权限  

```java
@Bean
public UserDetailsService userDetailsService() {
    //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
    manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
    return manager;
}
```

指定资源与权限的关系  

在controller中配置/r/r1需要p1权限，/r/r2需要p2权限。  

```java
@RestController
public class LoginController {
    ....
    @RequestMapping("/r/r1")
    @PreAuthorize("hasAuthority('p1')")//拥有p1权限方可访问
    public String r1(){
      return "访问r1资源";
    }
    
    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")//拥有p2权限方可访问
    public String r2(){
      return "访问r2资源";
    }
    ...

```

现在重启工程。

当访问以/r/开头的url时会判断用户是否认证，如果没有认证则跳转到登录页面，如果已经认证则判断用户是否具有该URL的访问权限，如果具有该URL的访问权限则继续，否则拒绝访问。

访问/r/r1，使用zhangsan登录可以正常访问，因为在/r/r1的方法上指定了权限p1，zhangsan用户拥有权限p1,所以可以正常访问。

访问/r/r1，使用lisi登录则拒绝访问，由于lisi用户不具有权限p1需要拒绝访问

注意：如果访问上不加@PreAuthorize，此方法没有授权控制。

## 工作原理

Spring Security所解决的问题就是**安全访问控制**，而安全访问控制功能其实就是对所有进入系统的请求进行拦截，校验每个请求是否能够访问它所期望的资源。根据前边知识的学习，可以通过Filter或AOP等技术来实现，Spring Security对Web资源的保护是靠Filter实现的  

当初始化Spring Security时，会创建一个名为SpringSecurityFilterChain的Servlet过滤器，类型为 org.springframework.security.web.FilterChainProxy，它实现了javax.servlet.Filter，因此外部的请求会经过此类  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-139.png)

FilterChainProxy是一个代理，真正起作用的是FilterChainProxy中SecurityFilterChain所包含的各个Filter，同时这些Filter作为Bean被Spring管理，它们是Spring Security核心，各有各的职责，但他们并不直接处理用户的**认证**，也不直接处理用户的**授权**，而是把它们交给了认证管理器（AuthenticationManager）和决策管理器（AccessDecisionManager）进行处理。

Spring Security功能的实现主要是由一系列过滤器链相互配合完成  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-140.png)

> **SecurityContextPersistenceFilter**  

整个拦截过程的入口和出口（也就是第一个和最后一个拦截器），会在请求开始时从配置好的 SecurityContextRepository 中获取 SecurityContext，然后把它设置给 SecurityContextHolder。在请求完成后将 SecurityContextHolder 持有的 SecurityContext 再保存到配置好的 SecurityContextRepository，同时清除 securityContextHolder 所持有的 SecurityContext；

> **UsernamePasswordAuthenticationFilter**   

用于处理来自表单提交的认证。该表单必须提供对应的用户名和密码，其内部还有登录成功或失败后进行处理的 AuthenticationSuccessHandler 和 AuthenticationFailureHandler，这些都可以根据需求做相关改变；

> **FilterSecurityInterceptor**   

用于保护web资源的，使用AccessDecisionManager对当前用户进行授权访问  

> **ExceptionTranslationFilter**   

能够捕获来自 FilterChain 所有的异常，并进行处理。但是它只会处理两类异常：AuthenticationException 和 AccessDeniedException，其它的异常它会继续抛出。

> Spring Security的执行流程 

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-141.png)

1. 用户提交用户名、密码被SecurityFilterChain中的UsernamePasswordAuthenticationFilter过滤器获取到，封装为请求Authentication，通常情况下是UsernamePasswordAuthenticationToken这个实现类  

2. 然后过滤器将Authentication提交至认证管理器（AuthenticationManager）进行认证

3.  认证成功后，AuthenticationManager身份管理器返回一个被填充满了信息的（包括上面提到的权限信息，身份信息，细节信息，但密码通常会被移除）Authentication实例  

4. SecurityContextHolder安全上下文容器将第3步填充了信息的Authentication，通过SecurityContextHolder.getContext().setAuthentication(…)方法，设置到其中。

5.  可以看出AuthenticationManager接口（认证管理器）是认证相关的核心接口，也是发起认证的出发点，它的实现类为ProviderManager。而Spring Security支持多种认证方式，因此ProviderManager维护着一个List<AuthenticationProvider>列表，存放多种认证方式，最终实际的认证工作是由AuthenticationProvider完成的。咱们知道web表单的对应的AuthenticationProvider实现类为DaoAuthenticationProvider，它的内部又维护着一个UserDetailsService负责UserDetails的获取。最终AuthenticationProvider将UserDetails填充至Authentication  

# OAuth2  

## OAuth2认证流程

微信扫码认证，这是一种第三方认证的方式，这种认证方式是基于OAuth2协议实现，

OAUTH协议为用户资源的授权提供了一个安全的、开放而又简易的标准。同时，任何第三方都可以使用OAUTH认证服务，任何服务提供商都可以实现自身的OAUTH认证服务，因而OAUTH是开放的。

业界提供了OAUTH的多种实现如PHP、JavaScript，Java，Ruby等各种语言开发包，大大节约了程序员的时间，因而OAUTH是简易的。互联网很多服务如Open API，很多大公司如Google，Yahoo，Microsoft等都提供了OAUTH认证服务，这些都足以说明OAUTH标准逐渐成为开放资源授权的标准。

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-142.png)

1. 用户点击微信扫码  

     用户进入黑马程序的登录页面，点击微信的图标开打微信扫码界面 ，微信扫码的目的是通过微信认证登录黑马程序员官网，黑马程序员网站需要从微信获取当前用户的身份信息才会让当前用户在黑马网站登录成功  

   **资源**：用户信息，在微信中存储。

   **资源拥有者**：用户是用户信息资源的拥有者。

   **认证服务**：微信负责认证当前用户的身份，负责为客户端颁发令牌。

   **客户端**：客户端会携带令牌请求微信获取用户信息，黑马程序员网站即客户端，黑马网站需要在浏览器打开

2.  用户授权黑马网站访问用户信息  

   资源拥有者扫描二维码表示资源拥有者请求微信进行认证，微信认证通过向用户手机返回授权页面 ，询问用户是否授权黑马程序员访问自己在微信的用户信息，用户点击“确认登录”表示同意授权，微信认证服务器会颁发一个授权码给黑马程序员的网站。只有资源拥有者同意微信才允许黑马网站访问资源。

3.  黑马程序员的网站获取到授权码  

4. 携带授权码请求微信认证服务器申请令牌  

5. 微信认证服务器向黑马程序员的网站响应令牌  

6. 黑马程序员网站请求微信资源服务器获取资源即用户信息  

​       黑马程序员网站携带令牌请求访问微信服务器获取用户的基本信息  

7. 资源服务器返回受保护资源即用户信息  

8. 黑马网站接收到用户信息，此时用户在黑马网站登录成功  

> Oauth2.0的认证流程  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-143.png)

Oauth2包括以下角色：

**客户端**：本身不存储资源，需要通过资源拥有者的授权去请求资源服务器的资源，比如：手机客户端、浏览器等。

上边示例中黑马网站即为客户端，它需要通过浏览器打开。

**资源拥有者**：通常为用户，也可以是应用程序，即该资源的拥有者。

A 表示 客户端请求资源拥有者授权。

B 表示 资源拥有者授权客户端即黑马网站访问自己的用户信息。

**授权服务器**（也称认证服务器）：认证服务器对资源拥有者进行认证，还会对客户端进行认证并颁发令牌。

C 客户端即黑马网站携带授权码请求认证。

D 认证通过颁发令牌。

**资源服务器**：存储资源的服务器。

E 表示客户端即黑马网站携带令牌请求资源服务器获取资源。

F 表示资源服务器校验令牌通过后提供受保护资源。

## OAuth2在本项目的应用

Oauth2是一个标准的开放的授权协议，应用程序可以根据自己的要求去使用Oauth2，本项目使用Oauth2实现如下目标：

**学成在线访问第三方系统的资源**。

本项目要接入微信扫码登录所以本项目要使用OAuth2协议访问微信中的用户信息。

**外部系统访问学成在线的资源** 。

同样当第三方系统想要访问学成在线网站的资源也可以基于OAuth2协议。

**学成在线前端**（客户端） 访问学成在线微服务的资源。

本项目是前后端分离架构，前端访问微服务资源也可以基于OAuth2协议进行认证。

## OAuth2的授权模式  

Spring Security支持OAuth2认证，OAuth2提供授权码模式、密码模式、简化模式、客户端模式等四种授权模式，前边举的微信扫码登录的例子就是基于授权码模式，这四种模式中授权码模式和密码模式应用较多  

### 授权码模式

OAuth2的几个授权模式是根据不同的应用场景以不同的方式去获取令牌，最终目的是要获取认证服务颁发的令牌，最终通过令牌去获取资源。

授权码模式简单理解是使用授权码去获取令牌，要想获取令牌先要获取授权码，授权码的获取需要资源拥有者亲自授权同意才可以获取。

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-144.png)

以黑马网站微信扫码登录为例进行说明：

1. 用户打开浏览器。

2. 通过浏览器访问客户端即黑马网站。

3. 用户通过浏览器向认证服务请求授权，请求授权时会携带客户端的URL，此URL为下发授权码的重定向地址。

4. 认证服务向资源拥有者返回授权页面。

5. 资源拥有者亲自授权同意。

6. 通过浏览器向认证服务发送授权同意。

7. 认证服务向客户端地址重定向并携带授权码。

8. 客户端即黑马网站收到授权码。

9. 客户端携带授权码向认证服务申请令牌。

10. 认证服务向客户端颁发令牌。

> 测试

要想测试授权模式首先要配置授权服务器即上图中的认证服务器，需要配置授权服务及令牌策略。

**1.配置AuthorizationServer.java、TokenConfig.java  **

> AuthorizationServer用 @EnableAuthorizationServer 注解标识并继承AuthorizationServerConfigurerAdapter来配置OAuth2.0 授权服务器。

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {
}
```

AuthorizationServerConfigurerAdapter要求配置以下几个类：

```java
public class AuthorizationServerConfigurerAdapter implements AuthorizationServerConfigurer {
    public AuthorizationServerConfigurerAdapter() {}
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {}
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {}
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {}
}
```

| 类                                         | 说明                                                         |
| ------------------------------------------ | ------------------------------------------------------------ |
| **ClientDetailsServiceConfigurer**         | 用来配置客户端详情服务（ClientDetailsService），（随便一个客户端都可以随便接入到它的认证服务吗？答案是否定的，服务提供商会给批准接入的客户端一个身份，用于接入时的凭据，有客户端标识和客户端秘钥，在这里配置批准接入的客户端的详细信息。） |
| **AuthorizationServerEndpointsConfigurer** | 用来配置令牌（token）的访问端点和令牌服务(token services)。  |
| **AuthorizationServerSecurityConfigurer**  | 用来配置令牌端点的安全约束.                                  |

**2.TokenConfig为令牌策略配置类**

暂时先使用InMemoryTokenStore在内存存储令牌，令牌的有效期等信息配置如下  

```java
//令牌管理服务
@Bean(name="authorizationServerTokenServicesCustom")
public AuthorizationServerTokenServices tokenService() {
    DefaultTokenServices service=new DefaultTokenServices();
    service.setSupportRefreshToken(true);//支持刷新令牌
    service.setTokenStore(tokenStore);//令牌存储策略
    service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期2小时
    service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期3天
    return service;
}
```

**3.配置认证管理bean**  

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    ....

```

> 重启认证服务

**1.GET请求获取授权码**

http://localhost:63070/auth/oauth/authorize?client_id=XcWebApp&response_type=code&scope=all&redirect_uri=http://www.51xuecheng.cn

参数列表如下：

•     client_id：客户端准入标识。

•     response_type：授权码模式固定为code。

•     scope：客户端权限。

•     redirect_uri：跳转uri，当授权码申请成功后会跳转到此地址，并在后边带上code参数（授权码）。

输入账号zhangsan、密码123登录成功，输入/oauth/authorize?client_id=XcWebApp&response_type=code&scope=all&redirect_uri=http://www.51xuecheng.cn

显示授权页面

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-145.png)

授权“XcWebApp”访问自己的受保护资源?

选择同意。

**2.请求成功**，重定向至http://www.51xuecheng.cn/?code=授权码，比如：http://www.51xuecheng.cn/?code=Wqjb5H



**3.使用httpclient工具post申请令牌**

/oauth/token?client_id=XcWebApp&client_secret=XcWebApp&grant_type=authorization_code&code=授权码&redirect_uri=http://www.51xuecheng.cn/

参数列表如下

•     client_id：客户端准入标识。

•     client_secret：客户端秘钥。

•     grant_type：授权类型，填写authorization_code，表示授权码模式

•     code：授权码，就是刚刚获取的授权码，注意：授权码只使用一次就无效了，需要重新申请。

•     redirect_uri：申请授权码时的跳转url，一定和申请授权码时用的redirect_uri一致。

httpclient脚本如下：

```json
### 授权码模式
### 第一步申请授权码(浏览器请求)/oauth/authorize?client_id=c1&response_type=code&scope=all&redirect_uri=http://www.51xuecheng.cn
### 第二步申请令牌
POST {{auth_host}}/auth/oauth/token?client_id=XcWebApp&client_secret=XcWebApp&grant_type=authorization_code&code=CTvCrB&redirect_uri=http://www.51xuecheng.cn
```

```json
{
  "access_token": "368b1ee7-a9ee-4e9a-aae6-0fcab243aad2",
  "token_type": "bearer",
  "refresh_token": "3d56e139-0ee6-4ace-8cbe-1311dfaa991f",
  "expires_in": 7199,
  "scope": "all"
}
```

说明：

1、access_token，访问令牌，用于访问资源使用。

2、token_type，bearer是在RFC6750中定义的一种token类型，在携带令牌访问资源时需要在head中加入bearer 空格 令牌内容

3、refresh_token，当令牌快过期时使用刷新令牌可以再次生成令牌。

4、expires_in：过期时间（秒）

5、scope，令牌的权限范围，服务端可以根据令牌的权限范围去对令牌授权。

### 密码模式

密码模式相对授权码模式简单，授权码模式需要借助浏览器供用户亲自授权，密码模式不用借助浏览器  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-146.png)

1、资源拥有者提供账号和密码

2、客户端向认证服务申请令牌，请求中携带账号和密码

3、认证服务校验账号和密码正确颁发令牌。

> 密码模式测试

1、POST请求获取令牌

/oauth/token?client_id=XcWebApp&client_secret=XcWebApp&grant_type=password&username=shangsan&password=123

参数列表如下：

•     client_id：客户端准入标识。

•     client_secret：客户端秘钥。

•     grant_type：授权类型，填写password表示密码模式

•     username：资源拥有者用户名。

•     password：资源拥有者密码。

2、授权服务器将令牌（access_token）发送给client

```json
### 密码模式
POST {{auth_host}}/auth/oauth/token?client_id=XcWebApp&client_secret=XcWebApp&grant_type=password&username=zhangsan&password=123
```

```json
{
  "access_token": "368b1ee7-a9ee-4e9a-aae6-0fcab243aad2",
  "token_type": "bearer",
  "refresh_token": "3d56e139-0ee6-4ace-8cbe-1311dfaa991f",
  "expires_in": 6806,
  "scope": "all"
}
```

> **本项目的应用方式**

通过演示授权码模式和密码模式，授权码模式适合客户端和认证服务非同一个系统的情况，所以**本项目使用授权码模式完成微信扫码认证**。本项目采用密码模式作为前端请求微服务的认证方式。

# JWT

## 普通令牌的问题

客户端申请到令牌，接下来客户端携带令牌去访问资源，到资源服务器将会校验令牌的合法性。

> 资源服务器如何校验令牌的合法性？以OAuth2的密码模式为例进行说明：

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-147.png)

从第4步开始说明：

4. 客户端携带令牌访问资源服务获取资源。

5. 资源服务远程请求认证服务校验令牌的合法性

6. 如果令牌合法资源服务向客户端返回资源。

> 上述普通令牌存在问题：

就是校验令牌需要远程请求认证服务，客户端的每次访问都会远程校验，执行性能低。

如果能够让资源服务自己校验令牌的合法性将省去远程请求认证服务的成本，提高了性能

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-148.png)

如何解决上边的问题，实现资源服务自行校验令牌。

令牌采用JWT格式即可解决上边的问题，用户认证通过后会得到一个JWT令牌，JWT令牌中已经包括了用户相关的信息，客户端只需要携带JWT访问资源服务，资源服务根据事先约定的算法自行完成令牌校验，无需每次都请求认证服务完成授权。



## 什么是JWT？

JSON Web Token（JWT）是一种**使用格式传递数据的网络令牌技术**，它是一个开放的行业标准（RFC 7519  ），它定义了一种简洁的、自包含的协议格式，用于在通信双方传递对象，传递的信息经过数字签名可以被验证和信任，它可以使用HMAC算法或使用RSA的公钥/私钥对来签名，防止内容篡改  

> 使用JWT可以实现无状态认证  

传统的基于session的方式是有状态认证，用户登录成功将用户的身份信息存储在服务端，这样加大了服务端的存储压力，并且这种方式不适合在分布式系统中应用。

如下图，当用户访问应用服务，每个应用服务都会去服务器查看session信息，如果session中没有该用户则说明用户没有登录，此时就会重新认证，而解决这个问题的方法是Session复制、Session黏贴  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-149.png)

如果是基于令牌技术在分布式系统中实现认证则服务端不用存储session，可以将用户身份信息存储在令牌中，用户认证通过后认证服务颁发令牌给用户，用户将令牌存储在客户端，去访问应用服务时携带令牌去访问，服务端从jwt解析出用户信息。这个过程就是无状态认证  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-150.png)

> JWT令牌的优点：

1、jwt基于json，非常方便解析。

2、可以在令牌中自定义丰富的内容，易扩展。

3、通过非对称加密算法及数字签名技术，JWT防止篡改，安全性高。

4、资源服务使用JWT可不依赖认证服务即可完成授权。

缺点：JWT令牌较长，占存储空间比较大。

> JWT令牌由三部分组成，每部分中间使用点（.）分隔  

1. **Header**    

    头部包括令牌的类型（即JWT）及使用的哈希算法（如HMAC SHA256或RSA），将下面的内容使用Base64Url编码，得到一个字符串就是JWT令牌的第一部分  

   ```json
   {
       "alg": "HS256",
       "typ": "JWT"
   }
   ```

2. **Payload**  

   第二部分是负载，内容也是一个json对象，它是存放有效信息的地方，它可以存放jwt提供的信息字段，比如：iss（签发者）,exp（过期时间戳）, sub（面向的用户）等，也可自定义字段  

    此部分不建议存放敏感信息，因为此部分可以解码还原原始内容。

    最后将第二部分负载使用Base64Url编码，得到一个字符串就是JWT令牌的第二部分。

   ```json
   {
       "sub": "1234567890",
       "name": "456",
       "admin": true
   }
   ```

3.  **Signature**  

   第三部分是签名，此部分用于防止jwt内容被篡改。

    这个部分使用base64url将前两部分进行编码，编码后使用点（.）连接组成字符串，最后使用header中声明的签名算法进行签名。

   ```json
   HMACSHA256(
       base64UrlEncode(header) + "." +
       base64UrlEncode(payload),
       secret)
   ```

base64UrlEncode(header)：jwt令牌的第一部分。

base64UrlEncode(payload)：jwt令牌的第二部分。

secret：签名所使用的密钥。

> 为什么JWT可以防止篡改？
>
> 第三部分使用签名算法对第一部分和第二部分的内容进行签名，常用的签名算法是 HS256，常见的还有md5,sha 等，签名算法需要使用密钥进行签名，密钥不对外公开，并且签名是不可逆的，如果第三方更改了内容那么服务器验证签名就会失败，要想保证验证签名正确必须保证内容、密钥与签名前一致。

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-151.png)

从上图可以看出认证服务和资源服务使用相同的密钥，这叫对称加密，对称加密效率高，如果一旦密钥泄露可以伪造jwt令牌。

JWT还可以使用非对称加密，认证服务自己保留私钥，将公钥下发给受信任的客户端、资源服务，公钥和私钥是配对的，成对的公钥和私钥才可以正常加密和解密，非对称加密效率低但相比对称加密非对称加密更安全一些。

## 测试生成JWT令牌

在认证服务中配置jwt令牌服务，即可实现生成jwt格式的令牌, 

```java
@Configuration
public class TokenConfig {

    private String SIGNING_KEY = "mq123";

    @Resource
    private TokenStore tokenStore;

    @Resource
    private JwtAccessTokenConverter accessTokenConverter;

    //    @Bean
//    public TokenStore tokenStore() {
//        // 使用内存存储令牌（普通令牌）
//        return new InMemoryTokenStore();
//    }
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(SIGNING_KEY);
        return converter;
    }

    // 令牌管理服务
    @Bean(name="authorizationServerTokenServicesCustom")
    public AuthorizationServerTokenServices tokenService() {
        DefaultTokenServices service=new DefaultTokenServices();
        service.setSupportRefreshToken(true);// 支持刷新令牌
        service.setTokenStore(tokenStore);// 令牌存储策略

        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter));
        service.setTokenEnhancer(tokenEnhancerChain);

        service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期2小时
        service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期3天
        return service;
    }

}
```

重启认证服务。

使用httpclient通过密码模式申请令牌

```json
### 密码模式
POST {{auth_host}}/oauth/token?client_id=XcWebApp&client_secret=XcWebApp&grant_type=password&username=zhangsan&password=123
```

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsieHVlY2hlbmctcGx1cyJdLCJ1c2VyX25hbWUiOiJ6aGFuZ3NhbiIsInNjb3BlIjpbImFsbCJdLCJleHAiOjE2ODg2NTY3NzMsImF1dGhvcml0aWVzIjpbInAxIl0sImp0aSI6ImFkYTg1MzNhLWQ2ZWMtNGMxOC04NTk3LWFlNTBjYzliNjE5MyIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.M2uIXawzIDSw3EId3EZHA4_OcMIwIhmKOJknZNWjnsQ",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsieHVlY2hlbmctcGx1cyJdLCJ1c2VyX25hbWUiOiJ6aGFuZ3NhbiIsInNjb3BlIjpbImFsbCJdLCJhdGkiOiJhZGE4NTMzYS1kNmVjLTRjMTgtODU5Ny1hZTUwY2M5YjYxOTMiLCJleHAiOjE2ODg5MDg3NzMsImF1dGhvcml0aWVzIjpbInAxIl0sImp0aSI6ImZlZTg3NDc4LTc1YTctNDU3My1hN2QzLTg2NTJlNGNhYWMzNiIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.LiWL2eATqcdaL9YF-oBeUZMOUsy7NYlrkMWounzlqhs",
  "expires_in": 7199,
  "scope": "all",
  "jti": "ada8533a-d6ec-4c18-8597-ae50cc9b6193"
}
```

1、access_token，生成的jwt令牌，用于访问资源使用。

2、token_type，bearer是在RFC6750中定义的一种token类型，在携带jwt访问资源时需要在head中加入bearer jwt令牌内容

3、refresh_token，当jwt令牌快过期时使用刷新令牌可以再次生成jwt令牌。

4、expires_in：过期时间（秒）

5、scope，令牌的权限范围，服务端可以根据令牌的权限范围去对令牌授权。

6、jti：令牌的唯一标识  

可以通过check_token接口校验jwt令牌

```json
POST {{auth_host}}/auth/oauth/check_token?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsieHVlY2hlbmctcGx1cyJdLCJ1c2VyX25hbWUiOiJ6aGFuZ3NhbiIsInNjb3BlIjpbImFsbCJdLCJleHAiOjE2ODg2NTY3NzMsImF1dGhvcml0aWVzIjpbInAxIl0sImp0aSI6ImFkYTg1MzNhLWQ2ZWMtNGMxOC04NTk3LWFlNTBjYzliNjE5MyIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.M2uIXawzIDSw3EId3EZHA4_OcMIwIhmKOJknZNWjnsQ
###
```

```json
{
  "aud": [
    "xuecheng-plus"
  ],
  "user_name": "zhangsan",
  "scope": [
    "all"
  ],
  "active": true,
  "exp": 1688656773,
  "authorities": [
    "p1"
  ],
  "jti": "ada8533a-d6ec-4c18-8597-ae50cc9b6193",
  "client_id": "XcWebApp"
}
```

## 测试资源服务校验令牌

拿到了jwt令牌下一步就要携带令牌去访问资源服务中的资源，本项目各个微服务就是资源服务，比如：内容管理服务，客户端申请到jwt令牌，携带jwt去内容管理服务查询课程信息，此时内容管理服务要对jwt进行校验，只有jwt合法才可以继续访问。  

![](https://cyan-images.oss-cn-shanghai.aliyuncs.com/images/online-education-20230122-152.png)

**1.在内容管理服务的content-api工程中添加依赖**

```xml
<!--认证相关-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
```

**2.在内容管理服务的content-api工程中添加TokenConfig**

```JAVA
@Configuration
public class TokenConfig {

    String SIGNING_KEY = "mq123";


//    @Bean
//    public TokenStore tokenStore() {
//        //使用内存存储令牌（普通令牌）
//        return new InMemoryTokenStore();
//    }

    @Resource
    private JwtAccessTokenConverter accessTokenConverter;

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(SIGNING_KEY);
        return converter;
    }

}
```

**3.添加资源服务配置**

```java
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ResouceServerConfig extends ResourceServerConfigurerAdapter {


    // 资源服务标识
    public static final String RESOURCE_ID = "xuecheng-plus";

    @Autowired
    TokenStore tokenStore;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID)//资源 id
                .tokenStore(tokenStore)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/r/**","/course/**").authenticated()//所有/r/**的请求必须认证通过
                .anyRequest().permitAll()
        ;
    }

}
```

根据配置可知/course/**开头的接口需要认证通过。

重启内容管理服务

**4.使用httpclient测试：**

访问根据课程id查询课程接口

```java
GET http://localhost:63040/content/course/2
```

```json
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}
```

从返回信息可知当前没有认证。

**5.下边携带JWT令牌访问接口：**

​	采用密码模式申请令牌。

​	携带jwt令牌访问资源服务地址

```json
### 携带token访问资源服务
GET http://localhost:63040/content/course/2
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsieHVlY2hlbmctcGx1cyJdLCJ1c2VyX25hbWUiOiJ6aGFuZ3NhbiIsInNjb3BlIjpbImFsbCJdLCJleHAiOjE2ODg2NTgzODcsImF1dGhvcml0aWVzIjpbInAxIl0sImp0aSI6IjE2MzRjNDkwLWRlNmYtNGRlMC04MjE3LTdlODU3MmE1MDYzNiIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.3jKDfLluAoJBrcOdQW8EXVMgM589Lj1uQmgj6GhGwG4
```

如果携带jwt令牌且jwt正确则正常访问资源服务的内容。

如果不正确则报令牌无效的错误：

```json
{
  "error": "invalid_token",
  "error_description": "Cannot convert access token to JSON"
}
```

## 测试获取用户身份

jwt令牌中记录了用户身份信息，当客户端携带jwt访问资源服务，资源服务验签通过后将前两部分的内容还原即可取出用户的身份信息，并**将用户身份信息放在了SecurityContextHolder上下文，SecurityContext与当前线程进行绑定，方便获取用户身份。**

还以查询课程接口为例，进入查询课程接口的代码中，添加获取用户身份的代码

```java
@ApiOperation("根据id查询课程信息接口")
@GetMapping("/course/{courseId}")
public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
    // 获取当前用户身份
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    System.out.println(">>>>>>>>>>>>>>>>>>>principal=: " + principal);
    return courseBaseService.getCourseBaseInfo(courseId);
}
```

测试时需要注意：

1、首先在资源服务配置中指定安全拦截机制 /course/开头的请求需要认证，即请求/course/{courseId}接口需要携带jwt令牌且签证通过。

2、认证服务生成jwt令牌将用户身份信息写入令牌，认证服务哪里获取用户身份。

目前还是将用户信息硬编码并暂放在内存中，如下：

```java
@Bean
public UserDetailsService userDetailsService() {
    //这里配置用户信息,这里暂时使用这种方式将用户存储在内存中
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername("zhangsan").password("123").authorities("p1").build());
    manager.createUser(User.withUsername("lisi").password("456").authorities("p2").build());
    return manager;
}
```

3、我们在使用密码模式生成jwt令牌时用的是zhangsan的信息，所以jwt令牌中存储了zhangsan的信息，那么在资源服务中应该取出zhangsan的信息才对。
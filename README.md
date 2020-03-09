# validator-lite-spring
[![validator-lite-spring](https://img.shields.io/badge/plugin-validator--lite--spring-green)](https://github.com/tangxbai/mybatis-mappe-spring) ![version](https://img.shields.io/badge/release-1.0.0-blue) [![maven central](https://img.shields.io/badge/maven%20central-1.0.0-brightgreen)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis) [![license](https://img.shields.io/badge/license-Apache%202.0-blue)](http://www.apache.org/licenses/LICENSE-2.0.html)

validator-lite关于spring的中间件，用于更方便的将validator-lite组件整合到spring框架中。

validator-lite-spring保留了原始spring的验证方式，支持 `@Validated` 注解开启数据验证，也支持方法`BindingResult` 参数捕获验证结果，不仅局限于JavaBean的合法性验证，更扩展到了普通参数也可以执行规则校验。

在项目中，你可以使用BindingResult来获取绑定的错误信息，也可以获取关于validator-lite（ValidatedResult）独有的验证结果，此对象中封装的验证结果更为详细明了。



## 关联文档

关于纯java环境，请移步到：https://github.com/tangxbai/validator-lite

关于整合springboot，请移步到：https://github.com/tangxbai/validator-lite-spring-boot



## 快速开始

```xml
<dependency>
    <groupId>com.viiyue.plugins</groupId>
    <artifactId>validator-lite-spring</artifactId>
    <version>[VERSION]</version>
</dependency>
```

如何获取最新版本？[点击这里获取最新版本](https://search.maven.org/search?q=g:com.viiyue.plugins%20AND%20a:validator-lite-spring&core=gav)



## 基础配置

仅需要配置一个SpringBean在你的mvc.xml中即可，让spring可以托管这个bean就可以开启验证支持。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:mvc="http://www.springframework.org/schema/mvc" 
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
		http://www.springframework.org/schema/context 
		http://www.springframework.org/schema/context/spring-context-4.0.xsd
		http://www.springframework.org/schema/mvc 
		http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd">
    
    <!-- 开启注解驱动模式 -->
    <mvc:annotation-driven/>
    
    <!-- 配置此bean即可开启数据验证支持 -->
    <bean class="com.viiyue.plugins.validator.spring.ValidatorLiteBean" primary="true">
        <property name="configuration.enableSingleMode" value="false"/>
        <property name="configuration.enableStrictMode" value="false"/>
        <property name="configuration.enableWarningLog" value="true"/>
        <property name="configuration.defaultLanguage" value="zh-TW"/>
    </bean>
    
    <!-- 开启Spring的国际化语言配置，此配置同样可以在validator-lite-spring中生效 -->
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="cacheSeconds" value="1800" />
        <property name="defaultEncoding" value="UTF-8" />
        <property name="useCodeAsDefaultMessage" value="true" />
        <property name="basenames">
            <list>
                <value>classpath:Message</value>
            </list>
        </property>
    </bean>
    
    <!-- 省略你关于其他bean的配置 -->
    ...
</beans>
```



## 关于作者

- 邮箱：tangxbai@hotmail.com
- 掘金： https://juejin.im/user/5da5621ce51d4524f007f35f
- 简书： https://www.jianshu.com/u/e62f4302c51f
- Issuse：https://github.com/tangxbai/validator-lite-spring/issues

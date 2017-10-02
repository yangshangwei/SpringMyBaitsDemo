# SpringMyBaitsDemo


Spring Mybatis整合demo. 
连接数据库访问数据

概述

使用Mybatis提供的ORM机制，面对具体的数据操作，Mybatis要求开发者编程具体的SQL语句。 相对于Hibernate等全自动的ORM机制而言，Mybatis在开发的工作量和数据库移植性上做出了让步，为数据持久化操作提供了更大的自由空间。

Mybatis的事务管理可以由Spring标准机制进行处理，它和Spring JDBC事务管理的方式完全一致，采用和SpringJDBC相同的DataSourceTransactionManager事务管理器。


配置SQLMapClient

每个Mybatis的应用程序都以一个SqlSessionFactory对象的实例为核心。 
SqlSessionFactory对象的实例可以通过SqlSessionFactoryBuilder对象来获得。 SqlSessionFactoryBuilder对象可以从XML配置文件或者Configuration类的实例中构建SqlSessionFactory对象。

和Hibernate相似，Mybatis拥有多个SQL映射文件，并通过一个配置文件对这些SQL映射文件进行装配，同时在该文件中定义一些控制属性的信息。

我们来看一个简单的Mybatis配置文件

myBatisConfig.xml

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
    PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <!-- (1) 提供可控制Mybatis框架运行行为的属性信息 -->
        <setting name="lazyLoadingEnabled" value="false" />
    </settings>
    <typeAliases>
        <!-- (2) 定义全限定类名的别名，在映射文件中可以通过别名代替具体的类名 -->
        <typeAlias alias="Artisan" type="com.artisan.domain.Artisan" />
    </typeAliases>
</configuration>

在（1）处提供提供可控制Mybatis框架运行行为的属性信息 ，在（2）处定义全限定类名的别名，在映射文件中可以通过别名代替具体的类名，简化配置。

当然我们也可以定义mappers标签引用SQL映射文件，如果我们在MyBatis的总装配置文件myBatisConfig.xml中指定SQL映射文件，则必须逐个列出所有的SQL映射文件，比较繁琐。我们更倾向于 通过SqlSessionFactoryBean提供的mapperLocations属性文件，通过扫描式加载SQL映射文件。 （下文描述）

我们来看下SQL映射文件的内容：

<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- （1）指定命名空间  -->
<mapper namespace="com.artisan.dao.mybatis.ArtisanMybatisDao">

 <insert id="addArtisan" parameterType="Artisan">
        INSERT INTO littel_artisan(artisan_id,artisan_name,artisan_desc)
        VALUES(#{artisanId},#{artisanName}, #{artisanDesc})
  </insert>

  <select id="selectArtisan" resultType="Artisan" parameterType="String">
        select artisan_id artisanId,
               artisan_name artisanName,
               artisan_desc artisanDesc
         from little_artisan
         where artisan_name = #{artisanName}
  </select>
</mapper>

该配置文件定义了Artisan实体类机型数据操作时所需要的SQL，同事还定义了查询结果和对象属性的映射关系。（这里我们仅仅列出一个查询示例）。

在（1）指定命名空间，每个具体的映射项都有一个id,可以通过命名空间和映射项的id定位到具体的映射项。

映射项的parameterType指定传入的参数对象，可以是全限定名的二类，也可以是类的别名（别名在Mybatis的主配置文件中定义）比如 <typeAlias alias="Artisan" type="com.artisan.domain.Artisan" />

如果映射项的入参是基础类型或者String类型，则可以通过使用int 、long、String的基础类型名。

如果映射项拥有返回类型对象，通过resultType指定。

在映射项中通过#{XXX}绑定parameterType指定参数类的属性，支持级联属性，比如{anotherDomain.anotherProperty}

在Spring中配置MyBatis

我们使用Mybatis提供的mybatis-spring整合类包实现Spring和Mybatis的整合。 我们添加将mybatis-spring构建pom.xml。

<!-- mybatis ORM框架 -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>${mybatis.version}</version>
        </dependency>

        <!-- mybatis-spring适配器 -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>${mybatis-spring.version}</version>
        </dependency>

我们来看下Spring配置Mybatis的配置文件 applicationContext-mybatis.xml

<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p" xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd">

    <!-- 扫描类包,将标注Spring注解的类自动转化Bean,同时完成Bean的注入 -->
    <context:component-scan base-package="com.artisan" 
                resource-pattern="**/mybatis/*.class"/>


    <!-- spring里使用org.mybatis.spring.mapper.MapperScannerConfigurer 进行自动扫描的时候,设置了sqlSessionFactory 
        的话,可能会导致PropertyPlaceholderConfigurer失效,也就是用${jdbc.username}这样之类的表达式,将无法获取到properties文件里的内容。 

        导致这一原因是因为,MapperScannerConigurer实际是在解析加载bean定义阶段的,这个时候要是设置sqlSessionFactory的话,会导致提前初始化一些类,
        这个时候PropertyPlaceholderConfigurer还没来得及替换定义中的变量,导致把表达式当作字符串复制了。
        但如果不设置sqlSessionFactory 属性的话,就必须要保证sessionFactory在spring中名称一定要是sqlSessionFactory,否则就无法自动注入。

        解决思路： 主要改动了MapperScannerConfigurer的配置,使用sqlSessionFactoryBeanName进行延迟注入 -->


    <!-- 如果Spring和Mybatis整合,要想使用context:property-placeholder.
        MapperScannerConfigurer中   p:sqlSessionFactoryBeanName="sqlSessionFactory" 这样配置-->
    <context:property-placeholder  
        location="classpath:spring/jdbc.properties"/>  


    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close" 
        p:driverClassName="${jdbc.driverClassName}"
        p:url="${jdbc.url}" 
        p:username="${jdbc.username}" 
        p:password="${jdbc.password}" />



    <!-- 通过Spring风格构建Mybatis的SqlSessionFactory -->
    <bean id="sqlSessionFactory"
          class="org.mybatis.spring.SqlSessionFactoryBean"
          p:dataSource-ref="dataSource"
          p:configLocation="classpath:mybatis/myBatisConfig.xml"
          p:mapperLocations="classpath:com/artisan/domain/mybatis/*.xml"/>


    <!-- 创建Mybatis访问数据库的模板类 -->
    <bean class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg ref="sqlSessionFactory"/>
    </bean>

    <!-- 查 找 类 路 径 下 的 映 射 器 并 自 动 将 它 们 创 建 成 MapperFactoryBean -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer"
          p:sqlSessionFactoryBeanName="sqlSessionFactory"
          p:basePackage="com.artisan.dao.mybatis"/>



    <!-- 暂未用到事务 -->
    <!-- Spring的事务管理器 
    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager"
          p:dataSource-ref="dataSource"/>-->

    <!-- 开启事务注解 
    <tx:annotation-driven transaction-manager="transactionManager"/>-->
</beans>

mybatis-spring类包提供了一个SqlSessionFactoryBean，以便通过Spring风格创建Mybatis的SqlSessionFactory。只需要注入数据源并指定Mybatis的总装配置文件那就可以啦。

我们通过 p:mapperLocations=”classpath:com/artisan/domain/mybatis/*.xml ，SqlSessionFactoryBean将扫描com/artisan/domain/mybatis类路径并加载所有以.xml为后缀的映射文件。

编写Mybatis的DAO

使用SqlSessionTemplate

mybatis-spring效仿Spring的风格提供了一个模板类SQLSessionTemplate,可以通过模板轻松的访问数据库。

首先在applicationContext-mybatis.xml中配置SQLSessionTemplate

    <!-- 创建Mybatis访问数据库的模板类 -->
    <bean class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg ref="sqlSessionFactory"/>
    </bean>

然后就可以通过SqlSessionTemplate调用SQL映射项完成数据访问操作

使用映射接口

Mybatis提供了一种可将SQL映射文件中的映射项通过名称匹配接口进行调用的方法： 接口名称和映射名空间相同，接口方法和映射元素的id相同。

我们在Artisan.xml的映射项中定义一个调用接口

<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- （1）指定命名空间  -->
<mapper namespace="com.artisan.dao.mybatis.ArtisanMybatisDao">

  <select id="selectArtisan" resultType="Artisan" parameterType="String">
        select artisan_id artisanId,
               artisan_name artisanName,
               artisan_desc artisanDesc
         from little_artisan
         where artisan_name = #{artisanName}
  </select>
</mapper>


定义同名接口

package com.artisan.dao.mybatis;

import com.artisan.domain.Artisan;

public interface ArtisanMybatisDao {

    Artisan selectArtisan(String artisanName);
}
1
2
3
4
5
6
7
8
9
接口名为com.artisan.dao.mybatis.ArtisanMybatisDao，Artisan.xml文件中的每个映射项对应一个接口方法，接口方法的嗯签名和映射项的声明匹配。

查询数据

package com.artisan.dao.mybatis;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.artisan.domain.Artisan;

/**
 * 
 * 
 * @ClassName: ArtisanMybatisDaoImpl
 * 
 * @Description: @Repository标注的DAO层，被Spring管理
 * 
 * @author: Mr.Yang
 * 
 * @date: 2017年10月1日 下午12:18:25
 */

@Repository
public class ArtisanMybatisDaoImpl implements ArtisanMybatisDao {

    private SqlSessionTemplate sqlSessionTemplate;

    // 注入SqlSessionTemplate
    @Autowired
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public Artisan selectArtisan(String artisanName) {
        // 必须是获取接口
        ArtisanMybatisDao artisanMybatisDao = sqlSessionTemplate
                .getMapper(ArtisanMybatisDao.class);
        return artisanMybatisDao.selectArtisan(artisanName);
    }

}

单元测试

package com.artisan.dao.mybatis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.artisan.domain.Artisan;

public class ArtisanMybatisDaoImplTest {

    ClassPathXmlApplicationContext ctx = null;
    ArtisanMybatisDaoImpl artisanMybatisDaoImpl = null;

    @Before
    public void initContext() {
        ctx = new ClassPathXmlApplicationContext(
                "spring/applicationContext-mybatis.xml");
        artisanMybatisDaoImpl = ctx.getBean("artisanMybatisDaoImpl",
                ArtisanMybatisDaoImpl.class);
    }

    @Test
    public void testSelectArtisanWithMybatis() {
        Artisan artisan = artisanMybatisDaoImpl.selectArtisan("artisan");
        System.out.println("Artisan Id:" + artisan.getArtisanId());
        System.out.println("Artisan Name:" + artisan.getArtisanName());
        System.out.println("Artisan Desc:" + artisan.getArtisanDesc());
    }

    @After
    public void releaseContext() {
        if (ctx != null) {
            ctx.close();
        }
    }
}

输出结果

十月 01, 2017 1:03:30 下午 org.springframework.context.support.ClassPathXmlApplicationContext prepareRefresh
信息: Refreshing org.springframework.context.support.ClassPathXmlApplicationContext@604b7b51: startup date [Sun Oct 01 13:03:30 BOT 2017]; root of context hierarchy
十月 01, 2017 1:03:31 下午 org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
信息: Loading XML bean definitions from class path resource [spring/applicationContext-mybatis.xml]
DEBUG LogFactory - Logging initialized using 'class org.apache.ibatis.logging.slf4j.Slf4jImpl' adapter.
DEBUG SqlSessionFactoryBean - Parsed configuration file: 'class path resource [mybatis/myBatisConfig.xml]'
DEBUG SqlSessionFactoryBean - Parsed mapper file: 'file [D:\workspace\workspace-sts\SpringMyBaitsDemo\target\classes\com\artisan\domain\mybatis\Artisan.xml]'
DEBUG SqlSessionUtils - Creating a new SqlSession
DEBUG SqlSessionUtils - SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@558cdd] was not registered for synchronization because synchronization is not active
DEBUG SpringManagedTransaction - JDBC Connection [jdbc:oracle:thin:@172.25.246.11:1521:testbed, UserName=CC, Oracle JDBC driver] will not be managed by Spring
DEBUG selectArtisan - ==>  Preparing: select artisan_id artisanId, artisan_name artisanName, artisan_desc artisanDesc from little_artisan where artisan_name = ? 
DEBUG selectArtisan - ==> Parameters: artisan(String)
DEBUG selectArtisan - <==      Total: 1
DEBUG SqlSessionUtils - Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@558cdd]
Artisan Id:AAAYbEAAZAAAK9fAAA
Artisan Name:artisan
Artisan Desc:Spring+MyBatis Demo
十月 01, 2017 1:03:36 下午 org.springframework.context.support.ClassPathXmlApplicationContext doClose
信息: Closing org.springframework.context.support.ClassPathXmlApplicationContext@604b7b51: startup date [Sun Oct 01 13:03:30 BOT 2017]; root of context hierarchy

可以看到从数据库中获取的数据如下： 
Artisan Id:AAAYbEAAZAAAK9fAAA 
Artisan Name:artisan 
Artisan Desc:Spring+MyBatis Demo

至此就完成了Spring+Mybatis的整合

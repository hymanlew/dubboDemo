package com.hyman.distributed.transaction.config.datasource;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Atomikos 事务管理
 *
 * JTA 实现原理：
 * 它包括事务管理器（Transaction Manager）和一个或多个支持 XA 协议的资源管理器 ( Resource Manager ) 两部分。可以将资源管理器
 * 看做任意类型的持久化数据存储，事务管理器则承担着所有事务参与单元的协调与控制。 根据所面向对象的不同，将 JTA 的事务管理器和资
 * 源管理器理解为两个方面：面向开发人员的使用接口（事务管理器）和面向服务提供商的实现接口（资源管理器）。
 *
 * 其中开发接口的主要部分为 UserTransaction 对象，开发人员通过此接口在信息系统中实现分布式事务；而实现接口则用来规范提供商（如
 * 数据库连接提供商）所提供的事务服务，它约定了事务的资源管理功能，使得 JTA 可以在异构事务资源之间执行协同沟通。
 * 以数据库为例，IBM 公司提供了实现分布式事务的数据库驱动程序（JDBC driver），Oracle 也提供了实现分布式事务的数据库驱动程序，在
 * 同时使用 DB2 和 Oracle 两种数据库连接时，JTA 即可以根据约定的接口协调者两种事务资源从而实现分布式事务。正是基于统一规范的不
 * 同实现使得 JTA 可以协调与控制不同数据库或者 JMS 厂商的事务资源。
 * 事务管理器（ TransactionManager ）将应用对分布式事务的使用映射到实际的事务资源并在事务资源间进行协调与控制。
 *
 * 面向开发人员的接口为 UserTransaction，开发人员通常只使用此接口实现 JTA 事务管理。
 * 面向提供商的实现接口主要涉及到 TransactionManager 和 Transaction 两个对象。Transaction 代表了一个物理意义上的事务（即数据
 * 库层面的事务），在开发人员调用 UserTransaction.begin() 方法时 TransactionManager 会创建一个 Transaction 事务对象（标志着
 * 事务的开始）并把此对象通过 ThreadLocale 关联到当前线程。UserTransaction 接口中的 commit()、rollback()，getStatus() 等方法
 * 都将最终委托给 Transaction 类的对应方法执行。
 * TransactionManager 本身并不承担实际的事务处理功能，它更多的是充当用户接口和实现接口之间的桥梁。
 *
 * UserTransaction，该接口定义的方法允许应用程序显式地管理事务边界，是 Java EE 中用来进行事务管理的一个接口。
 * 而在 JPA 中，JPA本身没有提供任何类型的声明式事务管理，所以为了更好的进行一系列的事务操作和管理，常用的事务管理有容器管
 * 理和人为管理。人为管理需要代码实现。在 JPA 中一种实现方式就是调用 entityManager 的 UserTransaction。
 *
 *
 * Spring本身并不支持事务实现，只是负责包装底层事务，应用底层支持什么样的事务策略，Spring就支持什么样的事务策略。
 *
 * Spring事务管理高层抽象主要有3个：
 * PlatformTransactionManager：  核心事务管理器(用来管理事务，包含事务的提交，回滚)，是Spring的事务管理器核心接口。
 * TransactionDefinition：       事务定义信息(隔离级别，传播方式，超时，是否只读)，该接口定义了一些基本事务属性。
 * TransactionStatus：           事务具体运行状态，是否是新的事物，是否有恢复点，是否为只回滚，是否已完成。
 *
 * 通过 @EnableTransactionManagement 来启用事务管理，该注解会自动查找满足条件的 PlatformTransactionManager。
 * 使用时，只需要在事务控制的地方加上 @Transactional 注解即可。
 *
 * @author hyman
 * @date 2019/8/13 3:41 PM
 */
@Configuration
@EnableTransactionManagement
public class TransactionManagerConfig {

    @Bean(name = "userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(10000);
        return userTransactionImp;
    }

    /**
     * 定义一个分布式的事务管理器（声明式的事务），实现了 UserTransaction 事务边界管理的接口。
     *
     * @return TransactionManager
     * @throws Throwable e
     */
    @Bean(name = "atomikosTransactionManager")
    public TransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    /**
     * 定义事务管理器（声明式的事务），总的一个对外暴露的事务管理器。
     * 使用 Spring @DependsOn 控制 bean 加载顺序（即先加载指定的 bean 对象），因为 spring 容器载入 bean 顺序是不确定的，
     * spring 框架没有约定特定顺序逻辑规范。
     *
     * @return PlatformTransactionManager
     * @throws Throwable e
     */
    @Bean(name = "transactionManager")
    @DependsOn({"userTransaction", "atomikosTransactionManager"})
    public PlatformTransactionManager transactionManager() throws Throwable {
        // 声明一个跨多个事务管理源的事务管理器
        return new JtaTransactionManager(userTransaction(), atomikosTransactionManager());
    }
}

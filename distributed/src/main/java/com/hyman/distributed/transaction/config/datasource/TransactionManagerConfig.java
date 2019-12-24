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
 * @author hyman
 * @date 2019/8/13 3:41 PM
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
 */
@Configuration
@EnableTransactionManagement
public class TransactionManagerConfig {

    /**
     * UserTransaction，该接口定义的方法允许应用程序显式地管理事务边界，是 Java EE 中用来进行事务管理的一个接口。
     * 而在 JPA 中，JPA本身没有提供任何类型的声明式事务管理，所以为了更好的进行一系列的事务操作和管理，常用的事务管理有容器管
     * 理和人为管理。人为管理需要代码实现。在 JPA 中一种实现方式就是调用 entityManager 的 UserTransaction。
     *
     * @return UserTransaction
     * @throws Throwable e
     */
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

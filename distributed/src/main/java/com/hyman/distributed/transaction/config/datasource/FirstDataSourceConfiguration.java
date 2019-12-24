package com.hyman.distributed.transaction.config.datasource;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.hyman.distributed.transaction.common.constant.DBConstants;
import com.mysql.cj.jdbc.MysqlXADataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author hyman
 * @date 2019/3/4  7:20 下午
 */
@Configuration
@MapperScan(basePackages = DBConstants.FIRST_MAPPER, sqlSessionFactoryRef = DBConstants.FIRST_SQL_SESSION_FACTORY)
@Slf4j
public class FirstDataSourceConfiguration {

    @Autowired
    private FirstDataSourceProperties firstDataSourceProperties;

    /**
     * 配置第一个数据源
     * @return
     */
    @Primary
    @Bean(DBConstants.FIRST_DATA_SOURCE)
    public DataSource firstDataSource() {

        // 使用 Druid 的分布式驱动，暂时不支持 MySql8 以上的版本
        //DruidXADataSource druidXADataSource = new DruidXADataSource();
        //BeanUtils.copyProperties(firstDataSourceProperties, druidXADataSource);

        // 使用 mysql 的分布式驱动，支持 MySql5.*、MySql8.* 以上版本
        MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
        mysqlXaDataSource.setUrl(firstDataSourceProperties.getUrl());
        mysqlXaDataSource.setPassword(firstDataSourceProperties.getPassword());
        mysqlXaDataSource.setUser(firstDataSourceProperties.getUsername());

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(mysqlXaDataSource);
        xaDataSource.setUniqueResourceName(DBConstants.FIRST_DATA_SOURCE);
        xaDataSource.setPoolSize(firstDataSourceProperties.getInitialSize());
        xaDataSource.setMinPoolSize(firstDataSourceProperties.getMinIdle());
        xaDataSource.setMaxPoolSize(firstDataSourceProperties.getMaxActive());
        xaDataSource.setMaxIdleTime(firstDataSourceProperties.getMinIdle());
        xaDataSource.setMaxLifetime(firstDataSourceProperties.getMinEvictableIdleTimeMillis());
        xaDataSource.setConcurrentConnectionValidation(firstDataSourceProperties.getTestWhileIdle());
        xaDataSource.setTestQuery(firstDataSourceProperties.getValidationQuery());

        return xaDataSource;
    }

    /**
     * 创建第一个 SqlSessionFactory（@Qualifier 按名字进行匹配）
     * @param firstDataSource
     * @return
     * @throws Exception
     */
    @Primary
    @Bean(DBConstants.FIRST_SQL_SESSION_FACTORY)
    public SqlSessionFactory firstSqlSessionFactory(@Qualifier(DBConstants.FIRST_DATA_SOURCE) DataSource firstDataSource)
            throws Exception {

        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(firstDataSource);
        // 设置 mapper 位置
        bean.setTypeAliasesPackage(DBConstants.FIRST_MAPPER);
        // 设置 mapper.xml 文件的路径
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(DBConstants.FIRST_MAPPER_XML));
        return bean.getObject();
    }
}

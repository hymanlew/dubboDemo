package com.hyman.distributed.transaction.common.constant;

/**
 * 数据库常量
 * @author hyman
 * @date 2019/3/8 4:13 下午
 */
public class DBConstants {

    /**
     * 数据源配置
     */
    public static final String FIRST_DATA_SOURCE = "firstDataSource";
    public static final String SECOND_DATA_SOURCE = "secondDataSource";

    /**
     * sqlSessionFactory
     */
    public static final String FIRST_SQL_SESSION_FACTORY = "firstSqlSessionFactory";
    public static final String SECOND_SQL_SESSION_FACTORY = "secondSqlSessionFactory";

    /**
     * mapper接口
     */
    public static final String FIRST_MAPPER = "com.hyman.distributed.transaction.dao.first.mapper";
    public static final String SECOND_MAPPER = "com.hyman.distributed.transaction.dao.second.mapper";
    /**
     * mapper.xml目录
     */
    public static final String FIRST_MAPPER_XML = "classpath:first-mapper/*.xml";
    public static final String SECOND_MAPPER_XML = "classpath:second-mapper/*.xml";
}

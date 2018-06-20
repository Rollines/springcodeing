package com.test.example.mycofig;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.test.example.dbconfig.DBConfig1;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20 上午10:16
 */
@SpringBootConfiguration
@MapperScan(basePackages = "com.test.example.code1",sqlSessionTemplateRef = "sqlSessionTemplate")
public class MyBatisConfig1 {
    // 配置数据源
    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource(DBConfig1 dbconfig) throws SQLException {
        MysqlXADataSource mysqlXADataSource = new MysqlXADataSource();
        mysqlXADataSource.setURL(dbconfig.getUrl());
        mysqlXADataSource.setPinGlobalTxToPhysicalConnection(true);
        mysqlXADataSource.setPassword(dbconfig.getPassword());
        mysqlXADataSource.setUser(dbconfig.getUsername());
        mysqlXADataSource.setPinGlobalTxToPhysicalConnection(true);

        AtomikosDataSourceBean atomikosData = new AtomikosDataSourceBean();
        atomikosData.setXaDataSource(mysqlXADataSource);
        atomikosData.setUniqueResourceName("dataSource");

        atomikosData.setMinPoolSize(dbconfig.getMinPoolSize());
        atomikosData.setMaxPoolSize(dbconfig.getMaxPoolSize());
        atomikosData.setMaxLifetime(dbconfig.getMaxLifetime());
        atomikosData.setBorrowConnectionTimeout(dbconfig.getBorrowConnectionTimeout());
        atomikosData.setLoginTimeout(dbconfig.getLoginTimeout());
        atomikosData.setMaintenanceInterval(dbconfig.getMaintenanceInterval());
        atomikosData.setMaxIdleTime(dbconfig.getMaxIdleTime());
        atomikosData.setTestQuery(dbconfig.getTestQuery());
        return atomikosData;
    }
    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Primary
    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

package org.ow2.proactive_grid_cloud_portal.fixture;

import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private final static Logger LOGGER = LoggerFactory.getLogger(Client.class);
    private static final String CONF_PROPERTIES = "conf.properties";
    private static Configuration config;

    public static void main(String[] args) throws ConfigurationException, Exception {
        try {
            config = new PropertiesConfiguration(CONF_PROPERTIES);
        } catch (ConfigurationException e) {
            throw new Exception("bad config", e);
        }
        initServer();
    }

    private static void initServer() {
        SimpleService service = new SimpleService();
        EssaiMessageBodyWriter writer = new EssaiMessageBodyWriter();
        
        ResteasyDeployment deployment = new ResteasyDeployment();

        int nettyPort = 8081;

        if (config != null) {
            deployment.setAsyncJobServiceEnabled(config.getBoolean("netty.asyncJobServiceEnabled", false));
            deployment.setAsyncJobServiceMaxJobResults(config.getInt("netty.asyncJobServiceMaxJobResults", 100));
            deployment.setAsyncJobServiceMaxWait(config.getLong("netty.asyncJobServiceMaxWait", 300000));
            deployment.setAsyncJobServiceThreadPoolSize(config.getInt("netty.asyncJobServiceThreadPoolSize", 100));

            nettyPort = config.getInt("netty.port", TestPortProvider.getPort());
        } else {
            LOGGER.warn("is going to use default netty config");
        }

        deployment.setResources(Arrays.<Object>asList(service));
        deployment.setProviders(Arrays.<Object>asList(writer));
        
        NettyJaxrsServer netty = new NettyJaxrsServer();
        netty.setDeployment(deployment);
        netty.setPort(nettyPort);
        netty.setRootResourcePath("");
        netty.setSecurityDomain(null);
        netty.start();
    }
}
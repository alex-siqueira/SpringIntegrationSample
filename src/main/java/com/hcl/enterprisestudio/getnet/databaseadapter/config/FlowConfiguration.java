package com.hcl.enterprisestudio.getnet.databaseadapter.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.jdbc.JdbcMessageHandler;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;


/**
 * Classe de configuracao do fluxo de integracao do Spring Integration <br/>
 * Aqui sao declarados os <i>Channels</i>, os <i>Adapters</i> e o <i>ServiceActivator</i> <br/>
 * <br/>
 * As propriedades de conexao usadas pelos Adapters de AMQP e JDBC estao no arquivo <i>application.properties</i>
 * 
 * @author Alexandre.Siqueira
 *
 */
@Configuration
public class FlowConfiguration {
	@Autowired
	private Environment env;
	
	
	/**
	 * Define o <i>Channel</i> de entrada para mensagens AMQP
	 * 
	 * @return canal de entrada para mensagens AMQP
	 */
  @Bean
  public MessageChannel rabbitInputChannel() {
      return new DirectChannel();
  }
	
  /**
   * Define o <i>Inbound Channel Adapter</i> que se conecta ao RabbitMQ
   * 
   * @param listenerContainer Parametros de conexao com a <i>Queue</i> AMQP
   * @param channel <i>Channel</i> onde as mensagens que chegarem serao publicadas
   * @return o <i>Inbound Channel Adapter</i>
   */
	@Bean
  public AmqpInboundChannelAdapter inboundAdapter(
  			SimpleMessageListenerContainer listenerContainer, 
  			@Qualifier("rabbitInputChannel") MessageChannel channel) {
		
      AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
      adapter.setOutputChannel(channel);
      return adapter;
  }
	
	/**
	 * Bean de configuracao da <i>Queue</i> AMQP
	 * 
	 * @param connectionFactory
	 * @return
	 */
  @Bean
  public SimpleMessageListenerContainer queueListenerContainer (ConnectionFactory connectionFactory) {
      SimpleMessageListenerContainer container =  new SimpleMessageListenerContainer(connectionFactory);
      container.setQueueNames("teste"); // AMQP Queue
      container.setConcurrentConsumers(2);
      return container;
  }

  
  /**
   * Servico de transformacao de mensagens AMQP para <i>PreparedStatements</i> JDBC
   * 
   * @param datasource
   * @return
   */
  @Bean
  @ServiceActivator(inputChannel = "rabbitInputChannel")
  public MessageHandler handler(DataSource datasource) {
  	JdbcMessageHandler messageHandler = new JdbcMessageHandler(datasource, "insert into teste values (?, ?)");
  	
  	messageHandler.setPreparedStatementSetter((ps, requestMessage) -> {
  			byte[] msgPayload = (byte[]) requestMessage.getPayload();
  			String payload = new String(msgPayload);
  			String[] param = payload.split(",");
				ps.setInt(1, Integer.parseInt(param[0]));
				ps.setString(2, param[1]);
		});
  	
  	return messageHandler;
  }
  
  /**
   * Configuração do DataSource JDBC
   * 
   * @return datasource
   */
  
  @Bean
  public DataSource getDataSource() {
  	DriverManagerDataSource manager = new DriverManagerDataSource();
  	manager.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
  	manager.setUrl(env.getProperty("spring.datasource.url"));
  	manager.setUsername(env.getProperty("spring.datasource.username"));
  	manager.setPassword(env.getProperty("spring.datasource.password"));
  	
  	//Especifico para Azure MySQL
  	Properties props = new Properties();
  	props.put("serverTimezone", "America/Sao_Paulo");
  	manager.setConnectionProperties(props);
  	
  	return manager;
  }
}

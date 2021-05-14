package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class GeoJsonBuilderConfig
{
	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(){
		return new Jackson2ObjectMapperBuilder().modules(  )
	}

}

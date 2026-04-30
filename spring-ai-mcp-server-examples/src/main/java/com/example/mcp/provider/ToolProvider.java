/* 
* Copyright 2025 - 2025 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 
package com.example.mcp.provider;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * MCP 工具提供者
 * 
 * 【关键点】
 * 1. 使用 @McpTool 注解声明 MCP 工具
 * 2. 使用 @McpToolParam 注解声明工具参数
 * 3. 提供天气查询功能作为示例
 * 4. 通过 RestClient 调用外部 API
 */
@Service
public class ToolProvider {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ToolProvider.class);

	private final RestClient restClient;

	public ToolProvider() {
		this.restClient = RestClient.create();
	}

	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	/**
	 * 获取特定位置的温度
	 * 
	 * 【关键点】
	 * 1. @McpTool 注解标记这是一个 MCP 工具
	 * 2. @McpToolParam 注解标记参数并提供描述
	 * 3. 使用 Open-Meteo API 获取实时天气数据
	 * 4. 记录日志便于调试
	 * 
	 * @param latitude 纬度
	 * @param longitude 经度
	 * @param city 城市名称
	 * @return 天气响应
	 */
	@McpTool(description = "Get the temperature (in celsius) for a specific location")
	public WeatherResponse getTemperature(@McpToolParam(description = "The location latitude") double latitude,
			@McpToolParam(description = "The location longitude") double longitude,
			@McpToolParam(description = "The city name") String city) {

		WeatherResponse response = restClient
			.get()
			.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
					latitude, longitude)
			.retrieve()
			.body(WeatherResponse.class);

		logger.info("Check temparature for {}. Lat: {}, Lon: {}. Temp: {}", city, latitude, longitude,
				response.current());

		return response;
	}
	
}

package tool;

import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeTools {

    @Tool(description = "获取用户时区的当前时间")
    public String getCurrentTime() {
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return now.toLocalTime().toString();
    }

    @Tool(description = "获取用户时区的当前日期和时间")
    public String getCurrentDateTime() {
        ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
        return now.toString();
    }

    @Tool(description = "获取指定城市的天气")
    public String getWeather(String city) {
        return city + "当前天气: 晴, 温度: 25°C";
    }
}

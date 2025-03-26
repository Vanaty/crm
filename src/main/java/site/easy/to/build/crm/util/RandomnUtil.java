package site.easy.to.build.crm.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class RandomnUtil {
    public static LocalDateTime getRandomDateTime() {
        Random random = new Random();
        
        // Define range (2000 to 2025)
        int minYear = 2000;
        int maxYear = 2025;
        
        // Generate random components
        int year = minYear + random.nextInt(maxYear - minYear + 1);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28); // Using 28 to avoid issues with different month lengths
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        
        // Create and return LocalDateTime
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    public static Object randomObject(List<Object> objects) {
        Random random = new Random();
        return objects.get(random.nextInt(objects.size()));
    }
}

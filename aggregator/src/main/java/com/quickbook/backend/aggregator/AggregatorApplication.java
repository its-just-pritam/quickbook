package com.quickbook.backend.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class AggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AggregatorApplication.class, args);
		System.out.println("""
				 $$$$$$\\                                                               $$\\                        \s
				$$  __$$\\                                                              $$ |                       \s
				$$ /  $$ | $$$$$$\\   $$$$$$\\   $$$$$$\\   $$$$$$\\   $$$$$$\\   $$$$$$\\ $$$$$$\\    $$$$$$\\   $$$$$$\\ \s
				$$$$$$$$ |$$  __$$\\ $$  __$$\\ $$  __$$\\ $$  __$$\\ $$  __$$\\  \\____$$\\\\_$$  _|  $$  __$$\\ $$  __$$\\\s
				$$  __$$ |$$ /  $$ |$$ /  $$ |$$ |  \\__|$$$$$$$$ |$$ /  $$ | $$$$$$$ | $$ |    $$ /  $$ |$$ |  \\__|
				$$ |  $$ |$$ |  $$ |$$ |  $$ |$$ |      $$   ____|$$ |  $$ |$$  __$$ | $$ |$$\\ $$ |  $$ |$$ |     \s
				$$ |  $$ |\\$$$$$$$ |\\$$$$$$$ |$$ |      \\$$$$$$$\\ \\$$$$$$$ |\\$$$$$$$ | \\$$$$  |\\$$$$$$  |$$ |     \s
				\\__|  \\__| \\____$$ | \\____$$ |\\__|       \\_______| \\____$$ | \\_______|  \\____/  \\______/ \\__|     \s
				          $$\\   $$ |$$\\   $$ |                    $$\\   $$ |                                      \s
				          \\$$$$$$  |\\$$$$$$  |                    \\$$$$$$  |                                      \s
				           \\______/  \\______/                      \\______/                                       \s
				""");
	}

}

package forum.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableJpaAuditing
@SpringBootApplication
public class HubApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(HubApplication.class, args);
	}

}

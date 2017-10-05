package cl.tastets.life.auth;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoDataAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.common.base.Predicate;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {MongoRepositoriesAutoConfiguration.class,
	MongoAutoConfiguration.class,
	MongoDataAutoConfiguration.class})
@EnableEurekaClient
@EnableCircuitBreaker
@EnableSwagger2
public class TastetsAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(TastetsAuthApplication.class, args);
	}

	@Autowired
	private Environment env;

	@Bean
	public MongoClient mongoClient() {
		return new MongoClient(new MongoClientURI(env.getProperty("auth.database.url")));
	}

	@Bean
	public MongoDatabase mongoDatabase() {
		MongoDatabase db = mongoClient().getDatabase(env.getProperty("auth.database.db"));
		return db;
	}

	
	@Bean
	public Docket apiGateway() {
		return new Docket(DocumentationType.SWAGGER_2)
				.groupName("auth")
				.apiInfo(apiInfo())
				.select()
				.paths(apiPaths())
				.build()
				.securitySchemes(Arrays.asList(new ApiKey("key", "api_key", "header")));
	}

	private Predicate<String> apiPaths() {
		return or(
				regex("/auth.*")
		);
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("Auth Microservice")
				.description("Microservicio de Autenticación/Autorización")
				.contact("Redd")
				.licenseUrl("http://www.gps.cl")
				.build();
	}

	@Bean
	public WebMvcConfigurerAdapter adapter() {
		return new WebMvcConfigurerAdapter() {

			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				if (!registry.hasMappingForPattern("/webjars/**")) {
					registry.addResourceHandler("/webjars/**").addResourceLocations(
							"classpath:/META-INF/resources/webjars/");
				}

			}
		};
	}
}

package br.com.fiap.satelite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCaching
@EnableFeignClients          // ← esta linha resolve o ClimaExternoClient
public class SateliteIngestionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SateliteIngestionServiceApplication.class, args);
	}
}
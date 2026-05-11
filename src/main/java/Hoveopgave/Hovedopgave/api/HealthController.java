package Hoveopgave.Hovedopgave.api;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("ok", "LiftLog", Instant.now().toString());
	}

	public record HealthResponse(String status, String application, String timestamp) {
	}
}

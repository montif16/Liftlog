package Hoveopgave.Hovedopgave.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseService;
import Hoveopgave.Hovedopgave.exercise.ExerciseService.ExerciseCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

	private final ExerciseService exerciseService;

	public ExerciseController(ExerciseService exerciseService) {
		this.exerciseService = exerciseService;
	}

	@GetMapping
	public List<ExerciseResponse> findAll() {
		return exerciseService.findAll().stream()
				.map(ExerciseController::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public ExerciseResponse findById(@PathVariable Long id) {
		return exerciseService.findById(id)
				.map(ExerciseController::toResponse)
				.orElseThrow(() -> notFound(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ExerciseResponse create(@Valid @RequestBody ExerciseUpsertRequest request) {
		return toResponse(exerciseService.create(toCommand(request)));
	}

	@PutMapping("/{id}")
	public ExerciseResponse update(@PathVariable Long id, @Valid @RequestBody ExerciseUpsertRequest request) {
		return exerciseService.update(id, toCommand(request))
				.map(ExerciseController::toResponse)
				.orElseThrow(() -> notFound(id));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		if (!exerciseService.delete(id)) {
			throw notFound(id);
		}
	}

	private static ResponseStatusException notFound(Long id) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found: " + id);
	}

	private static ExerciseCommand toCommand(ExerciseUpsertRequest request) {
		return new ExerciseCommand(request.name(), request.muscleGroup(), request.notes());
	}

	private static ExerciseResponse toResponse(Exercise exercise) {
		return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(), exercise.getNotes(),
				exercise.getCreatedAt().toString(), exercise.getUpdatedAt().toString());
	}

	public record ExerciseUpsertRequest(@NotBlank @Size(max = 120) String name,
			@NotBlank @Size(max = 80) String muscleGroup, @Size(max = 1000) String notes) {
	}

	public record ExerciseResponse(Long id, String name, String muscleGroup, String notes, String createdAt,
			String updatedAt) {
	}
}

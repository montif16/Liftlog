package Hoveopgave.Hovedopgave.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.session.TrainingSession;
import Hoveopgave.Hovedopgave.session.TrainingSessionExercise;
import Hoveopgave.Hovedopgave.session.TrainingSessionService;
import Hoveopgave.Hovedopgave.session.TrainingSessionService.TrainingSessionCommand;
import Hoveopgave.Hovedopgave.session.TrainingSessionService.TrainingSessionExerciseCommand;
import Hoveopgave.Hovedopgave.session.TrainingSessionService.TrainingSessionSetCommand;
import Hoveopgave.Hovedopgave.session.TrainingSessionSet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/sessions")
public class TrainingSessionController {

	private final TrainingSessionService sessionService;

	public TrainingSessionController(TrainingSessionService sessionService) {
		this.sessionService = sessionService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TrainingSessionResponse create(@Valid @RequestBody TrainingSessionCreateRequest request) {
		return toResponse(sessionService.create(toCommand(request)));
	}

	@GetMapping
	public List<TrainingSessionResponse> findAll() {
		return sessionService.findAll().stream()
				.map(TrainingSessionController::toResponse)
				.toList();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		if (!sessionService.delete(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id);
		}
	}

	private static TrainingSessionCommand toCommand(TrainingSessionCreateRequest request) {
		List<TrainingSessionExerciseCommand> exercises = request.exercises() == null ? List.of() : request.exercises()
				.stream()
				.map(exercise -> new TrainingSessionExerciseCommand(exercise.exerciseId(), exercise.orderIndex(),
						exercise.note(), toSetCommands(exercise.sets())))
				.toList();
		return new TrainingSessionCommand(request.templateId(), request.startedAt(), exercises);
	}

	private static List<TrainingSessionSetCommand> toSetCommands(List<TrainingSessionSetRequest> sets) {
		return sets == null ? List.of() : sets.stream()
				.map(set -> new TrainingSessionSetCommand(set.setNumber(), set.weight(), set.reps()))
				.toList();
	}

	private static TrainingSessionResponse toResponse(TrainingSession session) {
		List<TrainingSessionExerciseResponse> exercises = session.getExercises().stream()
				.sorted(Comparator.comparing(TrainingSessionExercise::getOrderIndex))
				.map(sessionExercise -> {
					List<TrainingSessionSetResponse> sets = sessionExercise.getSets().stream()
							.sorted(Comparator.comparing(TrainingSessionSet::getSetNumber))
							.map(sessionSet -> new TrainingSessionSetResponse(sessionSet.getId(),
									sessionSet.getSetNumber(), sessionSet.getWeight(), sessionSet.getReps()))
							.toList();

					Long exerciseId = sessionExercise.getExercise() == null ? null : sessionExercise.getExercise().getId();
					return new TrainingSessionExerciseResponse(sessionExercise.getId(), sessionExercise.getOrderIndex(),
							exerciseId, sessionExercise.getExerciseName(),
							sessionExercise.getMuscleGroup(), sessionExercise.getNote(), sets);
				})
				.toList();

		Long templateId = session.getTemplate() == null ? null : session.getTemplate().getId();
		return new TrainingSessionResponse(session.getId(), templateId, session.getTemplateName(),
				session.getStartedAt().toString(), session.getEndedAt().toString(), session.getDurationSeconds(),
				exercises);
	}

	public record TrainingSessionCreateRequest(@NotNull Long templateId, @NotNull Instant startedAt,
			@Valid List<TrainingSessionExerciseRequest> exercises) {
	}

	public record TrainingSessionExerciseRequest(@NotNull Long exerciseId, @NotNull @Min(1) Integer orderIndex,
			@Size(max = 1000) String note, @Valid List<TrainingSessionSetRequest> sets) {
	}

	public record TrainingSessionSetRequest(@NotNull @Min(1) Integer setNumber,
			@PositiveOrZero BigDecimal weight, @NotNull @Min(1) Integer reps) {
	}

	public record TrainingSessionResponse(Long id, Long templateId, String templateName, String startedAt,
			String endedAt, Long durationSeconds, List<TrainingSessionExerciseResponse> exercises) {
	}

	public record TrainingSessionExerciseResponse(Long id, Integer orderIndex, Long exerciseId, String exerciseName,
			String muscleGroup, String note, List<TrainingSessionSetResponse> sets) {
	}

	public record TrainingSessionSetResponse(Long id, Integer setNumber, BigDecimal weight, Integer reps) {
	}
}

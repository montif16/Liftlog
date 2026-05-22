package Hoveopgave.Hovedopgave.api;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.session.TrainingSession;
import Hoveopgave.Hovedopgave.session.TrainingSessionExercise;
import Hoveopgave.Hovedopgave.session.TrainingSessionRepository;
import Hoveopgave.Hovedopgave.session.TrainingSessionSet;
import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/sessions")
public class TrainingSessionController {

	private final TrainingSessionRepository sessionRepository;
	private final WorkoutTemplateRepository templateRepository;
	private final ExerciseRepository exerciseRepository;

	public TrainingSessionController(TrainingSessionRepository sessionRepository,
			WorkoutTemplateRepository templateRepository, ExerciseRepository exerciseRepository) {
		this.sessionRepository = sessionRepository;
		this.templateRepository = templateRepository;
		this.exerciseRepository = exerciseRepository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TrainingSessionResponse create(@Valid @RequestBody TrainingSessionCreateRequest request) {
		WorkoutTemplate template = templateRepository.findById(request.templateId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Template not found: " + request.templateId()));

		Instant endedAt = Instant.now();
		TrainingSession session = new TrainingSession();
		session.setTemplate(template);
		session.setTemplateName(template.getName());
		session.setStartedAt(request.startedAt());
		session.setEndedAt(endedAt);
		session.setDurationSeconds(Math.max(0, Duration.between(request.startedAt(), endedAt).getSeconds()));

		List<TrainingSessionExerciseRequest> exerciseRequests =
				request.exercises() == null ? List.of() : request.exercises();
		Map<Long, Exercise> exercises = loadExercises(exerciseRequests);

		exerciseRequests.stream().sorted(Comparator.comparing(TrainingSessionExerciseRequest::orderIndex))
				.forEach(exerciseRequest -> {
					Exercise exercise = exercises.get(exerciseRequest.exerciseId());
					TrainingSessionExercise sessionExercise = new TrainingSessionExercise();
					sessionExercise.setSession(session);
					sessionExercise.setExercise(exercise);
					sessionExercise.setOrderIndex(exerciseRequest.orderIndex());
					sessionExercise.setExerciseName(exercise.getName());
					sessionExercise.setMuscleGroup(exercise.getMuscleGroup());
					sessionExercise.setNote(normalizeNullable(exerciseRequest.note()));

					List<TrainingSessionSetRequest> setRequests =
							exerciseRequest.sets() == null ? List.of() : exerciseRequest.sets();
					setRequests.stream().sorted(Comparator.comparing(TrainingSessionSetRequest::setNumber))
							.forEach(setRequest -> {
								TrainingSessionSet sessionSet = new TrainingSessionSet();
								sessionSet.setSessionExercise(sessionExercise);
								sessionSet.setSetNumber(setRequest.setNumber());
								sessionSet.setWeight(setRequest.weight());
								sessionSet.setReps(setRequest.reps());
								sessionExercise.getSets().add(sessionSet);
							});

					session.getExercises().add(sessionExercise);
				});

		return toResponse(sessionRepository.save(session));
	}

	@GetMapping
	@Transactional(readOnly = true)
	public List<TrainingSessionResponse> findAll() {
		return sessionRepository.findAllByOrderByStartedAtDesc().stream()
				.map(TrainingSessionController::toResponse)
				.toList();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void delete(@PathVariable Long id) {
		TrainingSession session = sessionRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + id));

		sessionRepository.delete(session);
	}

	private Map<Long, Exercise> loadExercises(List<TrainingSessionExerciseRequest> exerciseRequests) {
		Set<Long> exerciseIds = new LinkedHashSet<>();
		for (TrainingSessionExerciseRequest exerciseRequest : exerciseRequests) {
			exerciseIds.add(exerciseRequest.exerciseId());
		}

		if (exerciseIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, Exercise> byId = new LinkedHashMap<>();
		for (Exercise exercise : exerciseRepository.findAllById(exerciseIds)) {
			byId.put(exercise.getId(), exercise);
		}

		if (byId.size() != exerciseIds.size()) {
			List<Long> missing = new ArrayList<>();
			for (Long exerciseId : exerciseIds) {
				if (!byId.containsKey(exerciseId)) {
					missing.add(exerciseId);
				}
			}
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown exercise IDs: " + missing);
		}

		return byId;
	}

	private static String normalizeNullable(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
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

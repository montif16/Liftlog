package Hoveopgave.Hovedopgave.session;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import Hoveopgave.Hovedopgave.exercise.ExerciseRepository;
import Hoveopgave.Hovedopgave.template.WorkoutTemplate;
import Hoveopgave.Hovedopgave.template.WorkoutTemplateRepository;

@Service
public class TrainingSessionService {

	private final TrainingSessionRepository sessionRepository;
	private final WorkoutTemplateRepository templateRepository;
	private final ExerciseRepository exerciseRepository;

	public TrainingSessionService(TrainingSessionRepository sessionRepository,
			WorkoutTemplateRepository templateRepository, ExerciseRepository exerciseRepository) {
		this.sessionRepository = sessionRepository;
		this.templateRepository = templateRepository;
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional
	public TrainingSession create(TrainingSessionCommand command) {
		WorkoutTemplate template = templateRepository.findById(command.templateId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Template not found: " + command.templateId()));

		Instant endedAt = Instant.now();
		TrainingSession session = new TrainingSession();
		session.setTemplate(template);
		session.setTemplateName(template.getName());
		session.setStartedAt(command.startedAt());
		session.setEndedAt(endedAt);
		session.setDurationSeconds(Math.max(0, Duration.between(command.startedAt(), endedAt).getSeconds()));

		List<TrainingSessionExerciseCommand> exerciseCommands =
				command.exercises() == null ? List.of() : command.exercises();
		Map<Long, Exercise> exercises = loadExercises(exerciseCommands);

		exerciseCommands.stream().sorted(Comparator.comparing(TrainingSessionExerciseCommand::orderIndex))
				.forEach(exerciseCommand -> {
					Exercise exercise = exercises.get(exerciseCommand.exerciseId());
					TrainingSessionExercise sessionExercise = new TrainingSessionExercise();
					sessionExercise.setSession(session);
					sessionExercise.setExercise(exercise);
					sessionExercise.setOrderIndex(exerciseCommand.orderIndex());
					sessionExercise.setExerciseName(exercise.getName());
					sessionExercise.setMuscleGroup(exercise.getMuscleGroup());
					sessionExercise.setNote(normalizeNullable(exerciseCommand.note()));

					List<TrainingSessionSetCommand> setCommands =
							exerciseCommand.sets() == null ? List.of() : exerciseCommand.sets();
					setCommands.stream().sorted(Comparator.comparing(TrainingSessionSetCommand::setNumber))
							.forEach(setCommand -> {
								TrainingSessionSet sessionSet = new TrainingSessionSet();
								sessionSet.setSessionExercise(sessionExercise);
								sessionSet.setSetNumber(setCommand.setNumber());
								sessionSet.setWeight(setCommand.weight());
								sessionSet.setReps(setCommand.reps());
								sessionExercise.getSets().add(sessionSet);
							});

					session.getExercises().add(sessionExercise);
				});

		return sessionRepository.save(session);
	}

	@Transactional(readOnly = true)
	public List<TrainingSession> findAll() {
		List<TrainingSession> sessions = sessionRepository.findAllByOrderByStartedAtDesc();
		sessions.forEach(TrainingSessionService::initializeHistoryData);
		return sessions;
	}

	@Transactional
	public boolean delete(Long id) {
		Optional<TrainingSession> session = sessionRepository.findById(id);
		if (session.isEmpty()) {
			return false;
		}

		sessionRepository.delete(session.get());
		return true;
	}

	private Map<Long, Exercise> loadExercises(List<TrainingSessionExerciseCommand> exerciseCommands) {
		Set<Long> exerciseIds = new LinkedHashSet<>();
		for (TrainingSessionExerciseCommand exerciseCommand : exerciseCommands) {
			exerciseIds.add(exerciseCommand.exerciseId());
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

	private static void initializeHistoryData(TrainingSession session) {
		if (session.getTemplate() != null) {
			session.getTemplate().getId();
		}
		session.getExercises().forEach(sessionExercise -> {
			if (sessionExercise.getExercise() != null) {
				sessionExercise.getExercise().getId();
			}
			sessionExercise.getSets().size();
		});
	}

	public record TrainingSessionCommand(Long templateId, Instant startedAt,
			List<TrainingSessionExerciseCommand> exercises) {
	}

	public record TrainingSessionExerciseCommand(Long exerciseId, Integer orderIndex, String note,
			List<TrainingSessionSetCommand> sets) {
	}

	public record TrainingSessionSetCommand(Integer setNumber, java.math.BigDecimal weight, Integer reps) {
	}
}

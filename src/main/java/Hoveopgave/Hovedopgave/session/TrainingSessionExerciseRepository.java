package Hoveopgave.Hovedopgave.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface TrainingSessionExerciseRepository extends JpaRepository<TrainingSessionExercise, Long> {

	@Modifying
	@Transactional
	@Query("update TrainingSessionExercise exercise set exercise.exercise = null where exercise.exercise.id = :exerciseId")
	void clearExerciseReferences(@Param("exerciseId") Long exerciseId);
}

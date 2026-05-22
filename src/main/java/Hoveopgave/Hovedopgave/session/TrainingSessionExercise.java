package Hoveopgave.Hovedopgave.session;

import Hoveopgave.Hovedopgave.exercise.Exercise;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "training_session_exercises")
@Getter
@Setter
@NoArgsConstructor
public class TrainingSessionExercise {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	private TrainingSession session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id")
	private Exercise exercise;

	@Column(nullable = false)
	private Integer orderIndex;

	@Column(nullable = false, length = 120)
	private String exerciseName;

	@Column(nullable = false, length = 80)
	private String muscleGroup;

	@Column(length = 1000)
	private String note;

	@OneToMany(mappedBy = "sessionExercise", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TrainingSessionSet> sets = new ArrayList<>();
}

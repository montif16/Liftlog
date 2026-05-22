package Hoveopgave.Hovedopgave.session;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "training_session_sets")
@Getter
@Setter
@NoArgsConstructor
public class TrainingSessionSet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "session_exercise_id", nullable = false)
	private TrainingSessionExercise sessionExercise;

	@Column(nullable = false)
	private Integer setNumber;

	@Column(precision = 6, scale = 2)
	private BigDecimal weight;

	@Column(nullable = false)
	private Integer reps;
}

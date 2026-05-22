import { useEffect, useState } from 'react'
import { getExerciseProgression, getExercises } from '../services/api'

function ProgressPage() {
  const [exercisesState, setExercisesState] = useState({
    loading: true,
    exercises: [],
    error: null,
  })
  const [selectedExerciseId, setSelectedExerciseId] = useState('')
  const [progressState, setProgressState] = useState({
    loading: false,
    progression: null,
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    async function loadExercises() {
      try {
        const exercises = await getExercises(controller.signal)
        setExercisesState({ loading: false, exercises, error: null })
        setSelectedExerciseId(exercises[0]?.id?.toString() ?? '')
      } catch (error) {
        if (error.name !== 'AbortError') {
          setExercisesState({ loading: false, exercises: [], error: error.message })
        }
      }
    }

    loadExercises()
    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!selectedExerciseId) {
      return undefined
    }

    const controller = new AbortController()

    async function loadProgression() {
      setProgressState({ loading: true, progression: null, error: null })

      try {
        const progression = await getExerciseProgression(selectedExerciseId, controller.signal)
        setProgressState({ loading: false, progression, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setProgressState({ loading: false, progression: null, error: error.message })
        }
      }
    }

    loadProgression()
    return () => controller.abort()
  }, [selectedExerciseId])

  function formatDate(value) {
    return new Intl.DateTimeFormat('en-GB', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value))
  }

  function formatWeight(value) {
    return value == null ? '-' : Number(value).toLocaleString('en-GB')
  }

  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">Progress</h2>
        <p className="text-secondary mb-3">
          Select an exercise to review saved session results over time.
        </p>

        {exercisesState.loading && <p className="mb-0">Loading exercises...</p>}

        {exercisesState.error && (
          <>
            <p className="status-pill error mb-2">Offline</p>
            <p className="text-danger mb-0">{exercisesState.error}</p>
          </>
        )}

        {!exercisesState.loading && !exercisesState.error && exercisesState.exercises.length === 0 && (
          <p className="mb-0">Create an exercise before tracking progress.</p>
        )}

        {!exercisesState.loading && !exercisesState.error && exercisesState.exercises.length > 0 && (
          <>
            <label className="form-label" htmlFor="progressExercise">
              Exercise
            </label>
            <select
              className="form-select mb-3"
              id="progressExercise"
              onChange={(event) => setSelectedExerciseId(event.target.value)}
              value={selectedExerciseId}
            >
              {exercisesState.exercises.map((exercise) => (
                <option key={exercise.id} value={exercise.id}>
                  {exercise.name}
                </option>
              ))}
            </select>

            {progressState.loading && <p className="mb-0">Loading progress...</p>}

            {progressState.error && <p className="text-danger mb-0">{progressState.error}</p>}

            {!progressState.loading &&
              !progressState.error &&
              progressState.progression?.entries.length === 0 && (
                <p className="mb-0">No saved sessions for this exercise yet.</p>
              )}

            {!progressState.loading &&
              !progressState.error &&
              progressState.progression?.entries.length > 0 && (
                <div className="d-grid gap-3">
                  {progressState.progression.entries.map((entry) => (
                    <article className="border rounded p-3" key={entry.sessionId}>
                      <div className="d-flex flex-column flex-md-row justify-content-md-between gap-2 mb-2">
                        <div>
                          <h3 className="h6 mb-1">{entry.templateName}</h3>
                          <p className="text-secondary small mb-0">{formatDate(entry.startedAt)}</p>
                        </div>
                        <div className="text-secondary small">
                          Volume: {formatWeight(entry.totalVolume)} kg
                        </div>
                      </div>

                      <p className="small mb-2">
                        Best set: {formatWeight(entry.bestWeight)} kg x {entry.bestReps ?? '-'}
                      </p>

                      <div className="d-flex flex-wrap gap-2">
                        {entry.sets.map((set) => (
                          <span className="border rounded px-2 py-1 small" key={set.setNumber}>
                            Set {set.setNumber}: {formatWeight(set.weight)} kg x {set.reps}
                          </span>
                        ))}
                      </div>
                    </article>
                  ))}
                </div>
              )}
          </>
        )}
      </div>
    </section>
  )
}

export default ProgressPage

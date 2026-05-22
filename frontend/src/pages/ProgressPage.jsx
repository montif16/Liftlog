import { useEffect, useState } from 'react'
import { getExerciseProgression, getExercises } from '../services/api'

function buildVolumeChart(entries) {
  const width = 640
  const height = 190
  const padding = 32
  const volumes = entries.map((entry) => Number(entry.totalVolume) || 0)
  const maxVolume = Math.max(...volumes, 1)
  const drawableWidth = width - padding * 2
  const drawableHeight = height - padding * 2

  const points = entries.map((entry, index) => {
    const x =
      entries.length === 1 ? width / 2 : padding + index * (drawableWidth / (entries.length - 1))
    const volume = Number(entry.totalVolume) || 0
    const y = height - padding - (volume / maxVolume) * drawableHeight

    return {
      x,
      y,
      volume,
      sessionId: entry.sessionId,
      startedAt: entry.startedAt,
    }
  })

  return {
    width,
    height,
    maxVolume,
    points,
    pointString: points.map((point) => `${point.x},${point.y}`).join(' '),
  }
}

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

  const progressionEntries = progressState.progression?.entries ?? []
  const volumeChart = buildVolumeChart(progressionEntries)
  const latestEntry = progressionEntries.at(-1)
  const newestProgressionEntries = [...progressionEntries].reverse()

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
              progressionEntries.length > 0 && (
                <div className="d-grid gap-3">
                  <div className="progress-chart border rounded p-3">
                    <div className="d-flex flex-column flex-md-row justify-content-md-between gap-1 mb-2">
                      <div>
                        <h3 className="h6 mb-1">Volume trend</h3>
                        <p className="text-secondary small mb-0">Total kg lifted per session</p>
                      </div>
                      <div className="text-secondary small">
                        Latest: {formatWeight(latestEntry?.totalVolume)} kg
                      </div>
                    </div>

                    <svg
                      aria-label="Volume trend for selected exercise"
                      className="progress-chart-svg"
                      role="img"
                      viewBox={`0 0 ${volumeChart.width} ${volumeChart.height}`}
                    >
                      <line
                        className="progress-chart-grid"
                        x1="32"
                        x2="608"
                        y1="32"
                        y2="32"
                      />
                      <line
                        className="progress-chart-grid"
                        x1="32"
                        x2="608"
                        y1="158"
                        y2="158"
                      />
                      <text className="progress-chart-label" x="32" y="22">
                        {formatWeight(volumeChart.maxVolume)} kg
                      </text>
                      <text className="progress-chart-label" x="32" y="184">
                        0 kg
                      </text>

                      {volumeChart.points.length > 1 && (
                        <polyline
                          className="progress-chart-line"
                          points={volumeChart.pointString}
                        />
                      )}

                      {volumeChart.points.map((point) => (
                        <circle
                          className="progress-chart-point"
                          cx={point.x}
                          cy={point.y}
                          key={point.sessionId}
                          r="5"
                        >
                          <title>
                            {formatDate(point.startedAt)}: {formatWeight(point.volume)} kg
                          </title>
                        </circle>
                      ))}
                    </svg>
                  </div>

                  {newestProgressionEntries.map((entry) => (
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

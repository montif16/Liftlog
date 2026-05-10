import { useEffect, useState } from 'react'
import { getExercises } from '../services/api'

function WorkoutsPage() {
  const [state, setState] = useState({
    loading: true,
    exercises: [],
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()

    async function loadExercises() {
      try {
        const exercises = await getExercises(controller.signal)
        setState({ loading: false, exercises, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setState({ loading: false, exercises: [], error: error.message })
        }
      }
    }

    loadExercises()
    return () => controller.abort()
  }, [])

  return (
    <section className="card shadow-sm">
      <div className="card-body">
        <h2 className="h4 mb-2">Workouts</h2>
        <p className="text-secondary mb-3">Exercise-listen hentes fra backend API.</p>

        {state.loading && <p className="mb-0">Loading exercises...</p>}

        {state.error && (
          <>
            <p className="status-pill error mb-2">Offline</p>
            <p className="text-danger mb-0">{state.error}</p>
          </>
        )}

        {!state.loading && !state.error && state.exercises.length === 0 && (
          <p className="mb-0">Ingen exercises endnu. Opret dem via API først.</p>
        )}

        {!state.loading && !state.error && state.exercises.length > 0 && (
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Muscle Group</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {state.exercises.map((exercise) => (
                  <tr key={exercise.id}>
                    <td>{exercise.name}</td>
                    <td>{exercise.muscleGroup}</td>
                    <td>{exercise.notes ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  )
}

export default WorkoutsPage

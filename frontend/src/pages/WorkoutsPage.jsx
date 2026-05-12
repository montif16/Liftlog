import { useEffect, useState } from 'react'
import { createWorkoutTemplate, getExercises, getWorkoutTemplates } from '../services/api'

const emptyForm = {
  name: '',
  description: '',
}

function WorkoutsPage() {
  const [state, setState] = useState({
    loading: true,
    templates: [],
    exercises: [],
    error: null,
  })
  const [form, setForm] = useState(emptyForm)
  const [selectedExercises, setSelectedExercises] = useState([])
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    async function loadWorkoutData() {
      try {
        const [templates, exercises] = await Promise.all([
          getWorkoutTemplates(controller.signal),
          getExercises(controller.signal),
        ])
        setState({ loading: false, templates, exercises, error: null })
      } catch (error) {
        if (error.name !== 'AbortError') {
          setState({ loading: false, templates: [], exercises: [], error: error.message })
        }
      }
    }

    loadWorkoutData()
    return () => controller.abort()
  }, [])

  function handleFieldChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function handleToggleExercise(exercise) {
    setSelectedExercises((current) => {
      const isAlreadySelected = current.some((item) => item.id === exercise.id)

      if (isAlreadySelected) {
        return current.filter((item) => item.id !== exercise.id)
      }

      return [...current, exercise]
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError(null)
    setSaving(true)

    try {
      const createdTemplate = await createWorkoutTemplate({
        name: form.name,
        description: form.description || null,
        items: [],
      })

      setState((current) => ({
        ...current,
        templates: [...current.templates, createdTemplate].sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }),
        ),
      }))
      setForm(emptyForm)
      setSelectedExercises([])
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="row g-3">
      <div className="col-12 col-lg-5">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Opret workout</h2>
            <p className="text-secondary mb-3">
              Vælg øvelser til workouten. Sets og reps tilføjes i næste iteration.
            </p>

            <form autoComplete="off" className="d-grid gap-3" onSubmit={handleSubmit}>
              <div>
                <label className="form-label" htmlFor="workout-name">
                  Navn
                </label>
                <input
                  autoComplete="off"
                  className="form-control"
                  id="workout-name"
                  maxLength={120}
                  name="name"
                  onChange={handleFieldChange}
                  placeholder="Upper Body Day"
                  required
                  value={form.name}
                />
              </div>

              <div>
                <label className="form-label" htmlFor="workout-description">
                  Beskrivelse
                </label>
                <textarea
                  className="form-control"
                  id="workout-description"
                  maxLength={1000}
                  name="description"
                  onChange={handleFieldChange}
                  rows={3}
                  value={form.description}
                />
              </div>

              <div>
                <h3 className="h6 text-uppercase text-secondary mb-3">Tilgængelige øvelser</h3>

                {state.loading && <p className="mb-0">Henter øvelser...</p>}

                {!state.loading && !state.error && state.exercises.length === 0 && (
                  <p className="mb-0 text-secondary">Opret øvelser før de kan tilføjes til workouts.</p>
                )}

                {!state.loading && !state.error && state.exercises.length > 0 && (
                  <div className="d-flex flex-wrap gap-2">
                    {state.exercises.map((exercise) => {
                      const isSelected = selectedExercises.some((item) => item.id === exercise.id)

                      return (
                        <button
                          className={`btn btn-sm ${isSelected ? 'btn-dark' : 'btn-outline-secondary'}`}
                          key={exercise.id}
                          onClick={() => handleToggleExercise(exercise)}
                          type="button"
                        >
                          {exercise.name}
                        </button>
                      )
                    })}
                  </div>
                )}
              </div>

              {formError && <p className="text-danger mb-0">{formError}</p>}

              <button className="btn btn-dark" disabled={saving} type="submit">
                {saving ? 'Gemmer...' : 'Gem workout'}
              </button>
            </form>
          </div>
        </div>
      </div>

      <div className="col-12 col-lg-7">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Workouts</h2>
            <p className="text-secondary mb-3">
              Workouts er færdige templates, for eksempel Upper Body Day, som senere kan
              bruges til at starte en aktiv session.
            </p>

            {state.loading && <p className="mb-0">Henter workouts...</p>}

            {state.error && (
              <>
                <p className="status-pill error mb-2">Offline</p>
                <p className="text-danger mb-0">{state.error}</p>
              </>
            )}

            {!state.loading && !state.error && state.templates.length === 0 && (
              <p className="mb-0">Ingen workouts endnu.</p>
            )}

            {!state.loading && !state.error && state.templates.length > 0 && (
              <div className="d-grid gap-3">
                {state.templates.map((template) => (
                  <article className="border rounded p-3" key={template.id}>
                    <div className="d-flex flex-column flex-md-row justify-content-md-between gap-2">
                      <div>
                        <h3 className="h5 mb-1">{template.name}</h3>
                        {template.description && (
                          <p className="text-secondary mb-0">{template.description}</p>
                        )}
                      </div>
                      <span className="text-secondary small">
                        {template.items.length} øvelser
                      </span>
                    </div>

                    {template.items.length > 0 && (
                      <div className="table-responsive mt-3">
                        <table className="table table-sm align-middle mb-0">
                          <thead>
                            <tr>
                              <th>Øvelse</th>
                              <th>Muskelgruppe</th>
                              <th>Sets</th>
                              <th>Reps</th>
                            </tr>
                          </thead>
                          <tbody>
                            {template.items.map((item) => (
                              <tr key={item.id}>
                                <td>{item.exerciseName}</td>
                                <td>{item.muscleGroup}</td>
                                <td>{item.targetSets}</td>
                                <td>{item.targetReps}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

export default WorkoutsPage

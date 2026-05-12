import { useEffect, useState } from 'react'
import { Trash2 } from 'lucide-react'
import { createExercise, deleteExercise, getExercises } from '../services/api'

const emptyForm = {
  exerciseName: '',
  muscleGroup: '',
  notes: '',
}

const muscleGroups = [
  'Chest',
  'Back',
  'Lats',
  'Shoulders',
  'Biceps',
  'Triceps',
  'Legs',
  'Glutes',
  'Core',
  'Full body',
]

function ExercisesPage() {
  const [state, setState] = useState({
    loading: true,
    exercises: [],
    error: null,
  })
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)
  const [deletingId, setDeletingId] = useState(null)
  const [confirmingDeleteId, setConfirmingDeleteId] = useState(null)
  const [deleteError, setDeleteError] = useState(null)

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

  function handleFieldChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError(null)
    setSaving(true)

    try {
      const createdExercise = await createExercise({
        name: form.exerciseName,
        muscleGroup: form.muscleGroup,
        notes: form.notes || null,
      })

      setState((current) => ({
        ...current,
        exercises: [...current.exercises, createdExercise].sort((a, b) =>
          a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }),
        ),
      }))
      setForm(emptyForm)
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(exercise) {
    setDeleteError(null)
    setDeletingId(exercise.id)

    try {
      await deleteExercise(exercise.id)
      setState((current) => ({
        ...current,
        exercises: current.exercises.filter((item) => item.id !== exercise.id),
      }))
      setConfirmingDeleteId(null)
    } catch (error) {
      setDeleteError(error.message)
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <section className="row g-3">
      <div className="col-12 col-lg-5">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Opret øvelse</h2>
            <p className="text-secondary mb-3">
              Øvelser gemmes i backend og kan senere bruges i workout templates.
            </p>

            <form autoComplete="off" className="d-grid gap-3" onSubmit={handleSubmit}>
              <div>
                <label className="form-label" htmlFor="exercise-name">
                  Øvelse
                </label>
                <input
                  autoComplete="off"
                  className="form-control"
                  id="exercise-name"
                  maxLength={120}
                  name="exerciseName"
                  onChange={handleFieldChange}
                  required
                  value={form.exerciseName}
                />
              </div>

              <div>
                <label className="form-label" htmlFor="exercise-muscle-group">
                  Muskelgruppe
                </label>
                <select
                  className="form-control"
                  id="exercise-muscle-group"
                  name="muscleGroup"
                  onChange={handleFieldChange}
                  required
                  value={form.muscleGroup}
                >
                  <option value="">Vælg muskelgruppe</option>
                  {muscleGroups.map((muscleGroup) => (
                    <option key={muscleGroup} value={muscleGroup}>
                      {muscleGroup}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="form-label" htmlFor="exercise-notes">
                  Noter
                </label>
                <textarea
                  className="form-control"
                  id="exercise-notes"
                  maxLength={1000}
                  name="notes"
                  onChange={handleFieldChange}
                  rows={3}
                  value={form.notes}
                />
              </div>

              {formError && <p className="text-danger mb-0">{formError}</p>}

              <button className="btn btn-dark" disabled={saving} type="submit">
                {saving ? 'Gemmer...' : 'Gem øvelse'}
              </button>
            </form>
          </div>
        </div>
      </div>

      <div className="col-12 col-lg-7">
        <div className="card shadow-sm h-100">
          <div className="card-body">
            <h2 className="h4 mb-2">Øvelser</h2>
            <p className="text-secondary mb-3">Listen hentes fra backend API.</p>

            {state.loading && <p className="mb-0">Henter øvelser...</p>}

            {deleteError && <p className="text-danger mb-3">{deleteError}</p>}

            {state.error && (
              <>
                <p className="status-pill error mb-2">Offline</p>
                <p className="text-danger mb-0">{state.error}</p>
              </>
            )}

            {!state.loading && !state.error && state.exercises.length === 0 && (
              <p className="mb-0">Ingen øvelser endnu.</p>
            )}

            {!state.loading && !state.error && state.exercises.length > 0 && (
              <div className="table-responsive">
                <table className="table table-sm align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Navn</th>
                      <th>Muskelgruppe</th>
                      <th>Noter</th>
                      <th className="text-end">Handlinger</th>
                    </tr>
                  </thead>
                  <tbody>
                    {state.exercises.map((exercise) => (
                      <tr key={exercise.id}>
                        <td>{exercise.name}</td>
                        <td>{exercise.muscleGroup}</td>
                        <td>{exercise.notes ?? '-'}</td>
                        <td className="text-end">
                          {confirmingDeleteId === exercise.id ? (
                            <div className="d-flex justify-content-end gap-2">
                              <button
                                className="btn btn-sm btn-danger"
                                disabled={deletingId === exercise.id}
                                onClick={() => handleDelete(exercise)}
                                type="button"
                              >
                                {deletingId === exercise.id ? 'Sletter...' : 'Bekræft'}
                              </button>
                              <button
                                className="btn btn-sm btn-outline-secondary"
                                disabled={deletingId === exercise.id}
                                onClick={() => setConfirmingDeleteId(null)}
                                type="button"
                              >
                                Annuller
                              </button>
                            </div>
                          ) : (
                            <button
                              aria-label={`Slet øvelsen ${exercise.name}`}
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => setConfirmingDeleteId(exercise.id)}
                              title="Slet øvelse"
                              type="button"
                            >
                              <Trash2 aria-hidden="true" size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  )
}

export default ExercisesPage

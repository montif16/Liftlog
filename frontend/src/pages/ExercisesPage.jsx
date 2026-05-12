import { useEffect, useRef, useState } from 'react'
import { Trash2 } from 'lucide-react'
import { createExercise, deleteExercise, getExercises, updateExercise } from '../services/api'

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
  const skipBlurSaveRef = useRef(false)
  const [state, setState] = useState({
    loading: true,
    exercises: [],
    error: null,
  })
  const [form, setForm] = useState(emptyForm)
  const [editingCell, setEditingCell] = useState(null)
  const [savingCellKey, setSavingCellKey] = useState(null)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)
  const [inlineEditError, setInlineEditError] = useState(null)
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

  function startInlineEdit(exercise, field) {
    setEditingCell({
      exerciseId: exercise.id,
      field,
      value: exercise[field] ?? '',
    })
    setInlineEditError(null)
    setConfirmingDeleteId(null)
  }

  function handleInlineEditChange(event) {
    const { value } = event.target
    setEditingCell((current) => ({ ...current, value }))
  }

  async function saveInlineEdit(exercise) {
    if (!editingCell) {
      return
    }

    const editedCell = editingCell
    const trimmedValue = editedCell.value.trim()

    if ((editedCell.field === 'name' || editedCell.field === 'muscleGroup') && !trimmedValue) {
      setInlineEditError('Navn og muskelgruppe må ikke være tomme.')
      return
    }

    const payload = {
      name: exercise.name,
      muscleGroup: exercise.muscleGroup,
      notes: exercise.notes ?? null,
      [editedCell.field]: editedCell.field === 'notes' ? trimmedValue || null : trimmedValue,
    }

    const cellKey = `${exercise.id}-${editedCell.field}`
    setInlineEditError(null)
    setSavingCellKey(cellKey)

    try {
      const updatedExercise = await updateExercise(exercise.id, payload)

      setState((current) => ({
        ...current,
        exercises: current.exercises
          .map((item) => (item.id === exercise.id ? updatedExercise : item))
          .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' })),
      }))
      setEditingCell((current) =>
        current?.exerciseId === editedCell.exerciseId && current.field === editedCell.field
          ? null
          : current,
      )
    } catch (error) {
      setInlineEditError(error.message)
    } finally {
      setSavingCellKey(null)
    }
  }

  function handleInlineEditKeyDown(event) {
    if (event.key === 'Enter') {
      event.preventDefault()
      event.currentTarget.blur()
    }

    if (event.key === 'Escape') {
      skipBlurSaveRef.current = true
      setEditingCell(null)
      setInlineEditError(null)
    }
  }

  function handleInlineEditBlur(exercise) {
    if (skipBlurSaveRef.current) {
      skipBlurSaveRef.current = false
      return
    }

    saveInlineEdit(exercise)
  }

  function renderEditableCell(exercise, field) {
    const isEditing = editingCell?.exerciseId === exercise.id && editingCell.field === field
    const cellKey = `${exercise.id}-${field}`
    const displayValue = exercise[field] || '-'

    if (!isEditing) {
      return (
        <button
          className="editable-cell"
          onClick={() => startInlineEdit(exercise, field)}
          type="button"
        >
          {displayValue}
        </button>
      )
    }

    if (field === 'muscleGroup') {
      return (
        <select
          autoFocus
          className="form-control form-control-sm"
          disabled={savingCellKey === cellKey}
          onBlur={() => handleInlineEditBlur(exercise)}
          onChange={handleInlineEditChange}
          onKeyDown={handleInlineEditKeyDown}
          value={editingCell.value}
        >
          {muscleGroups.map((muscleGroup) => (
            <option key={muscleGroup} value={muscleGroup}>
              {muscleGroup}
            </option>
          ))}
        </select>
      )
    }

    return (
      <input
        autoFocus
        className="form-control form-control-sm"
        disabled={savingCellKey === cellKey}
        maxLength={field === 'notes' ? 1000 : 120}
        onBlur={() => handleInlineEditBlur(exercise)}
        onChange={handleInlineEditChange}
        onKeyDown={handleInlineEditKeyDown}
        value={editingCell.value}
      />
    )
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
      if (editingCell?.exerciseId === exercise.id) {
        setEditingCell(null)
      }
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

            {inlineEditError && <p className="text-danger mb-3">{inlineEditError}</p>}

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
                      <tr className="exercise-row" key={exercise.id}>
                        <td>{renderEditableCell(exercise, 'name')}</td>
                        <td>{renderEditableCell(exercise, 'muscleGroup')}</td>
                        <td>{renderEditableCell(exercise, 'notes')}</td>
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
                              className="btn btn-sm btn-outline-danger exercise-delete-button"
                              onClick={() => {
                                setConfirmingDeleteId(exercise.id)
                                setEditingCell(null)
                              }}
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

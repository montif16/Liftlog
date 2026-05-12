const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function getHealth(signal) {
  const response = await fetch(`${API_BASE_URL}/api/health`, { signal })

  if (!response.ok) {
    throw new Error(`Health request failed: ${response.status}`)
  }

  return response.json()
}

export async function getExercises(signal) {
  const response = await fetch(`${API_BASE_URL}/api/exercises`, { signal })

  if (!response.ok) {
    throw new Error(`Exercises request failed: ${response.status}`)
  }

  return response.json()
}

export async function getWorkoutTemplates(signal) {
  const response = await fetch(`${API_BASE_URL}/api/templates`, { signal })

  if (!response.ok) {
    throw new Error(`Workout templates request failed: ${response.status}`)
  }

  return response.json()
}

export async function createWorkoutTemplate(template) {
  const response = await fetch(`${API_BASE_URL}/api/templates`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(template),
  })

  if (!response.ok) {
    throw new Error(`Create workout template request failed: ${response.status}`)
  }

  return response.json()
}

export async function createExercise(exercise) {
  const response = await fetch(`${API_BASE_URL}/api/exercises`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(exercise),
  })

  if (!response.ok) {
    throw new Error(`Create exercise request failed: ${response.status}`)
  }

  return response.json()
}

export async function deleteExercise(id) {
  const response = await fetch(`${API_BASE_URL}/api/exercises/${id}`, {
    method: 'DELETE',
  })

  if (!response.ok) {
    throw new Error(`Delete exercise request failed: ${response.status}`)
  }
}

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

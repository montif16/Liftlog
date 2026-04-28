import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './layouts/AppLayout'
import HistoryPage from './pages/HistoryPage'
import HomePage from './pages/HomePage'
import ProgressPage from './pages/ProgressPage'
import SessionPage from './pages/SessionPage'
import WorkoutsPage from './pages/WorkoutsPage'

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="/workouts" element={<WorkoutsPage />} />
        <Route path="/session" element={<SessionPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/progress" element={<ProgressPage />} />
        <Route path="*" element={<Navigate replace to="/" />} />
      </Route>
    </Routes>
  )
}

export default App

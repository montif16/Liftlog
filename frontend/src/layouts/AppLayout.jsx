import { Outlet } from 'react-router'
import MainNav from '../components/MainNav'

function AppLayout() {
  return (
    <>
      <MainNav />
      <main className="liftlog-main">
        <div className="container">
          <Outlet />
        </div>
      </main>
    </>
  )
}

export default AppLayout

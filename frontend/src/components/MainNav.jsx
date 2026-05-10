import { NavLink } from 'react-router'

const navItems = [
  { to: '/', label: 'Home' },
  { to: '/workouts', label: 'Workouts' },
  { to: '/session', label: 'Session' },
  { to: '/history', label: 'History' },
  { to: '/progress', label: 'Progress' },
]

function MainNav() {
  return (
    <header className="bg-white border-bottom">
      <div className="container py-3">
        <div className="d-flex flex-column flex-md-row align-items-md-center gap-3">
          <h1 className="h5 mb-0">LiftLog</h1>
          <nav className="d-flex flex-wrap gap-2">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                className={({ isActive }) =>
                  `btn btn-sm ${isActive ? 'btn-dark' : 'btn-outline-secondary'}`
                }
                end={item.to === '/'}
                to={item.to}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </div>
    </header>
  )
}

export default MainNav

import { NavLink } from "react-router";
import ThemeToggle from "./ThemeToggle";

export default function Navbar() {
  return (
    <nav className="bg-base-300 p-2 flex justify-between">
      <div className="flex gap-2">
        <NavLink
          to="/"
          className={({ isActive }) =>
            isActive ? "nav-buttons btn-active" : "nav-buttons"
          }
        >
          Home
        </NavLink>
        <NavLink
          to="/about"
          className={({ isActive }) =>
            isActive ? "nav-buttons btn-active" : "nav-buttons"
          }
        >
          About
        </NavLink>
        <NavLink
          to="/jobs-page"
          className={({ isActive }) =>
            isActive ? "nav-buttons btn-active" : "nav-buttons"
          }
        >
          Job Tracker
        </NavLink>
      </div>
      <div className="space-x-4">
        <ThemeToggle />
        <NavLink
          to="/login"
          className="btn btn-accent text-2xl text-accent-content"
        >
          Login
        </NavLink>
      </div>
    </nav>
  );
}

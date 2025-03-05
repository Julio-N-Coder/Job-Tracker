import { NavLink } from "react-router";
import ThemeToggle from "./ThemeToggle";
import { useNavigate } from "react-router";

export default function Navbar() {
  let navigation = useNavigate();

  function signOut() {
    localStorage.removeItem("token");

    navigation("/");
  }

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
        {!localStorage.token ? (
          <NavLink
            to="/login"
            className="btn btn-accent text-2xl text-accent-content"
          >
            Login
          </NavLink>
        ) : (
          <button
            onClick={signOut}
            className="btn btn-accent text-xl text-accent-content"
          >
            SignOut
          </button>
        )}
      </div>
    </nav>
  );
}

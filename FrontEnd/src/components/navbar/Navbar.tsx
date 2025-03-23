import { NavLink } from "react-router";
import ThemeToggle from "./ThemeToggle";
import { useNavigate } from "react-router";
import { UserSvg } from "../svg/Svg";

export default function Navbar() {
  let navigation = useNavigate();

  function signOut() {
    localStorage.removeItem("token");
    localStorage.removeItem("refresh_token");

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
      <div className="flex items-center gap-2">
        <ThemeToggle />
        {!localStorage.token ? (
          <NavLink
            to="/login"
            className="btn btn-accent text-2xl text-accent-content"
          >
            Login
          </NavLink>
        ) : (
          <UserIcon signOut={signOut} />
        )}
      </div>
    </nav>
  );
}

function UserIcon({ signOut }: { signOut: () => void }) {
  return (
    <div className="dropdown dropdown-end">
      <div
        tabIndex={0}
        role="button"
        className="bg-base-100 p-3 rounded-full hover:bg-base-200"
      >
        <UserSvg className="h-6 opacity-80" />
      </div>
      <ul
        tabIndex={0}
        className="dropdown-content menu bg-base-100 rounded-box z-1 w-52 p-2 shadow-sm font-bold"
      >
        <li>
          <button onClick={signOut}>Sign Out</button>
        </li>
        <li>
          <NavLink to="/profile-page">Profile</NavLink>
        </li>
      </ul>
    </div>
  );
}

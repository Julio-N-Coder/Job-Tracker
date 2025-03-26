import { NavLink } from "react-router";
import ThemeToggle from "./ThemeToggle";
import { useNavigate } from "react-router";
import { UserSvg, SmallNavDropDownSvg } from "../svg/Svg";

export default function Navbar() {
  let navigation = useNavigate();

  function signOut() {
    localStorage.removeItem("token");
    localStorage.removeItem("refresh_token");

    navigation("/");
  }

  return (
    <nav className="bg-base-300 p-2 flex justify-between">
      <div className="navbar-start md:hidden">
        <div className="dropdown">
          <div tabIndex={0} role="button" className="btn btn-circle">
            <SmallNavDropDownSvg />
          </div>
          <div
            tabIndex={0}
            className="menu menu-sm p-1 dropdown-content bg-base-100 rounded-box z-1 w-52 shadow border-1"
          >
            <PageNavLinks
              className="small-nav-buttons"
              activeClassName="small-nav-buttons btn-active"
            />
          </div>
        </div>
      </div>
      <div className="md:flex gap-2 hidden">
        <PageNavLinks
          className="nav-buttons"
          activeClassName="nav-buttons btn-active"
        />
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

function PageNavLinks({
  className,
  activeClassName,
}: {
  className: string;
  activeClassName: string;
}) {
  return (
    <>
      <NavLink
        to="/"
        className={({ isActive }) => (isActive ? activeClassName : className)}
      >
        Home
      </NavLink>
      <NavLink
        to="/about"
        className={({ isActive }) => (isActive ? activeClassName : className)}
      >
        About
      </NavLink>
      <NavLink
        to="/jobs-page"
        className={({ isActive }) => (isActive ? activeClassName : className)}
      >
        Job Tracker
      </NavLink>
    </>
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

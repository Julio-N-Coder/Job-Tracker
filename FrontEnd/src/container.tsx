import { Outlet } from "react-router";
import Navbar from "./components/navbar/Navbar";

export default function Container() {
  return (
    <div className="antialiased">
      <Navbar />
      <Outlet />
    </div>
  );
}

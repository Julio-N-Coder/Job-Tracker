import { Outlet } from "react-router";
import Navbar from "./components/navbar/Navbar";

export default function Container({ className }: { className?: string }) {
  return (
    <div className={`antialiased ${className}`}>
      <Navbar />
      <Outlet />
    </div>
  );
}

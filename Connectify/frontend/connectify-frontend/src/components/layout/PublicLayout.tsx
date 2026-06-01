import { Outlet } from "react-router-dom";
import TopNavbar from "./TopNavbar";

export default function PublicLayout() {
  return (
    <div className="min-h-screen bg-gray-900">

      {/* ONLY TOP NAVBAR */}
      <TopNavbar />

      {/* PAGE CONTENT */}
      <Outlet />

    </div>
  );
}
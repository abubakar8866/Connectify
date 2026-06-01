import { Outlet } from "react-router-dom";

import TopNavbar from "./TopNavbar";
import Sidebar from "./Sidebar";

export default function MainLayout() {
  return (
    <div className="min-h-screen bg-gray-900">

      <TopNavbar />

      <div className="flex">

        <Sidebar />

        <main className="flex-1 p-6">
          <Outlet />
        </main>

      </div>

    </div>
  );

}


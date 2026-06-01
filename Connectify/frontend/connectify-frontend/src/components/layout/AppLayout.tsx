import type { ReactNode } from "react";

import TopNavbar from "./TopNavbar";
import Sidebar from "./Sidebar";

interface AppLayoutProps {
  children: ReactNode;
}

export default function AppLayout({
  children,
}: AppLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-900">

      <TopNavbar />

      <div className="flex">

        <Sidebar />

        <main className="flex-1 p-6">
          {children}
        </main>

      </div>

    </div>
  );

}


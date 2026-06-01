import { useState } from "react";

import {
  FaBars,
  FaTimes,
} from "react-icons/fa";

import SidebarItem from "./SidebarItem";

import { useAppSelector } from "../../hooks/reduxHooks";

import {
  guestMenu,
  userMenu,
  adminMenu,
} from "../../constants/sidebarMenus";

export default function Sidebar() {

  const [collapsed, setCollapsed] =
    useState(false);

  const user = useAppSelector(
    (state) => state.auth.user
  );

  let menuItems = guestMenu;

  if (user) {

    if (user.role === "ADMIN") {
      menuItems = adminMenu;
    } else {
      menuItems = userMenu;
    }

  }

  return (
    <aside
      className={`
        bg-gray-800
        border-r
        border-gray-700
        min-h-[calc(100vh-64px)]
        transition-all
        duration-300
        ${collapsed ? "w-20" : "w-64"}
      `}
    >

      <div className="p-4">

        <button
          onClick={() =>
            setCollapsed(!collapsed)
          }
          className="text-white text-xl"
        >
          {collapsed
            ? <FaBars />
            : <FaTimes />}
        </button>

      </div>

      <nav className="flex flex-col gap-2 px-3">

        {menuItems.map((item) => (
          <SidebarItem
            key={item.path}
            to={item.path}
            label={item.label}
            icon={item.icon}
            collapsed={collapsed}
          />
        ))}

      </nav>

    </aside>
  );

}


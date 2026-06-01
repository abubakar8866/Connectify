import type { IconType } from "react-icons";
import { NavLink } from "react-router-dom";

interface SidebarItemProps {
  to: string;
  label: string;
  icon: IconType;
  collapsed: boolean;
}

export default function SidebarItem({
  to,
  label,
  icon: Icon,
  collapsed,
}: SidebarItemProps) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `
        flex
        items-center
        gap-3
        p-3
        rounded-lg
        text-white
        hover:bg-gray-700
        transition-colors
        ${
          isActive
            ? "bg-green-600"
            : ""
        }
      `
      }
    >
      <Icon />

      {!collapsed && (
        <span>{label}</span>
      )}
    </NavLink>
  );

}


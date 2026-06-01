import {
  FaHome,
  FaSignInAlt,
  FaUserPlus,
  FaUser,
  FaBookmark,
  FaComments,
  FaImage,
  FaUsers,
  FaFlag,
  FaSignOutAlt,
  FaUserShield,
} from "react-icons/fa";

export const guestMenu = [
  {
    label: "Feed",
    path: "/",
    icon: FaHome,
  },
  {
    label: "Login",
    path: "/login",
    icon: FaSignInAlt,
  },
  {
    label: "Register",
    path: "/register",
    icon: FaUserPlus,
  },
];

export const userMenu = [
  {
    label: "Feed",
    path: "/",
    icon: FaHome,
  },
  {
    label: "Profile",
    path: "/profile",
    icon: FaUser,
  },
  {
    label: "Posts",
    path: "/posts",
    icon: FaImage,
  },
  {
    label: "Stories",
    path: "/stories",
    icon: FaImage,
  },
  {
    label: "Saved",
    path: "/saved-posts",
    icon: FaBookmark,
  },
  {
    label: "Users",
    path: "/users",
    icon: FaUsers,
  },
  {
    label: "Search",
    path: "/search",
    icon: FaUsers,
  },
  {
    label: "Chat",
    path: "/chat",
    icon: FaComments,
  },
  {
    label: "Reports",
    path: "/reports",
    icon: FaFlag,
  },
];

export const adminMenu = [
  ...userMenu,

  {
    label: "Admin Panel",
    path: "/admin",
    icon: FaUserShield,
  },
];
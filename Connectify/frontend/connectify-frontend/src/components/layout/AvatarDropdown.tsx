import { useState } from "react";
import { useAppDispatch } from "../../hooks/reduxHooks";
import { logout } from "../../features/auth/authSlice";
import { clearAuth } from "../../utils/authStorage";
import { logoutApi } from "../../api/authApi";
import { useNavigate } from "react-router-dom";
import { showSuccess, showError } from "../../utils/toast";

interface AvatarDropdownProps {
  name: string;
  profileImageUrl?: string | null;
}

export default function AvatarDropdown({
  name,
  profileImageUrl,
}: AvatarDropdownProps) {

  const [open, setOpen] =  useState(false);
  const [loading, setLoading] = useState(false);

  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      setLoading(true);

      await logoutApi();

      dispatch(logout());

      clearAuth();

      setOpen(false);

      showSuccess("You are logout successfully.");

      navigate("/login");

    } catch (err) {
      showError(
          err?.response?.data?.message || "Something went wrong"
      );
    } finally{
        setLoading(false);
    }
  };

  return (
    <div className="relative">

      <button
        onClick={() => setOpen(!open)}
        className="
          flex
          items-center
          gap-2
          text-white
        "
      >

        {profileImageUrl ? (

          <img
            src={profileImageUrl}
            alt={name}
            className="
              w-10
              h-10
              rounded-full
              object-cover
              border
              border-gray-600
            "
          />

        ) : (

          <div
            className="
              w-10
              h-10
              rounded-full
              bg-green-600
              flex
              items-center
              justify-center
              font-bold
            "
          >
            {name.charAt(0).toUpperCase()}
          </div>

        )}

        <span>{name}</span>

      </button>

      {open && (
        <div
          className="
            absolute
            right-0
            mt-2
            w-48
            bg-gray-800
            border
            border-gray-700
            rounded-lg
            shadow-lg
            z-50
          "
        >

          <button
            className="
              w-full
              text-left
              px-4
              py-3
              text-white
              hover:bg-gray-700
            "
          >
            Profile
          </button>

          <button
            onClick={handleLogout}
            className="
              w-full
              text-left
              px-4
              py-3
              text-red-400
              hover:bg-gray-700
            "
          >
            Logout
          </button>

        </div>
      )}

    </div>
  );

}


import { FaBell } from "react-icons/fa";

import AvatarDropdown from "./AvatarDropdown";

import { useAppSelector } from "../../hooks/reduxHooks";

export default function TopNavbar() {

  const user = useAppSelector(
    (state) => state.auth.user
  );

  return (
    <header
      className="
        h-16
        border-b
        border-gray-700
        bg-gray-800
        flex
        items-center
        justify-between
        px-6
      "
    >

      {/* LEFT */}
      <div className="flex items-center gap-2">

        <div
          className="
            w-12
            h-12
            rounded-full
            bg-green-600
            flex
            items-center
            justify-center
            text-white
            text-2xl
            font-bold
          "
        >
          C
        </div>

        <h1
          className="
            text-white
            text-2xl
            font-bold
          "
        >
          Connectify
        </h1>

      </div>

      {/* RIGHT */}
      {user && (

        <div className="flex items-center gap-6">

          <AvatarDropdown
            name={user.name}
            profileImageUrl={
              user.profileImageUrl
            }
          />

          <button
            className="
              relative
              text-white
              text-xl
            "
          >
            <FaBell />

            <span
              className="
                absolute
                -top-2
                -right-2
                bg-red-500
                text-white
                text-xs
                rounded-full
                w-5
                h-5
                flex
                items-center
                justify-center
              "
            >
              5
            </span>

          </button>

        </div>

      )}

    </header>
  );

}


import { useEffect } from "react";
import { useAppDispatch } from "../hooks/reduxHooks";
import { hydrateAuth } from "../features/auth/authSlice";

export default function AuthInitializer() {

  const dispatch = useAppDispatch();

  useEffect(() => {

    const auth =
      localStorage.getItem("auth");

    if (auth) {

      const parsed =
        JSON.parse(auth);

      dispatch(
        hydrateAuth({
          user: parsed.user,
          accessToken: parsed.accessToken,
          refreshToken: parsed.refreshToken,
        })
      );
    }

  }, [dispatch]);

  return null;
}


import { create } from "zustand";
import { persist } from "zustand/middleware";

const useAuthStore = create(
    persist(
        (set) => ({
            token: null,

            user: null,

            isAuthenticated: false,

            login: (token, user) =>
                set({
                    token,
                    user,
                    isAuthenticated: true,
                }),

            logout: () =>
                set({
                    token: null,
                    user: null,
                    isAuthenticated: false,
                }),

            updateUser: (user) =>
                set((state) => ({
                    ...state,
                    user,
                })),
        }),
        {
            name: "auth-storage",
        }
    )
);

export default useAuthStore;
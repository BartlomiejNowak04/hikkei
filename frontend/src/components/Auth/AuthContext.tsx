"use client";
import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

type User = {
    name: string;
    avatar?: string;
};

type AuthContextValue = {
    isLogged: boolean;
    user: User | null;
    login: (token: string, user?: Partial<User>) => void;
    logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [token, setToken] = useState<string | null>(null);
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        const t = typeof window !== "undefined" ? localStorage.getItem("token") : null;
        const u = typeof window !== "undefined" ? localStorage.getItem("user") : null;
        if (t) setToken(t);
        if (u) {
            try { setUser(JSON.parse(u)); } catch {}
        }
    }, []);

    const login = (t: string, u?: Partial<User>) => {
        setToken(t);
        localStorage.setItem("token", t);
        if (u) {
            const full = { name: u.name ?? "Użytkownik", avatar: u.avatar };
            setUser(full);
            localStorage.setItem("user", JSON.stringify(full));
        }
    };

    const logout = () => {
        setToken(null);
        setUser(null);
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.assign("/");
    };

    const value = useMemo(
        () => ({ isLogged: Boolean(token), user, login, logout }),
        [token, user]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within <AuthProvider>");
    return ctx;
}

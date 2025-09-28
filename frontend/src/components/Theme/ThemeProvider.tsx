"use client";
import React, {
    createContext,
    useContext,
    useEffect,
    useState,
    useCallback,
} from "react";

type Theme = "light" | "dark";

type ThemeContextType = {
    theme: Theme;
    toggleTheme: () => void;
    setTheme: (t: Theme) => void;
};

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
    const [theme, setThemeState] = useState<Theme>("light");

    const apply = useCallback((t: Theme) => {
        document.documentElement.setAttribute("data-theme", t);
    }, []);

    const setTheme = useCallback(
        (t: Theme) => {
            setThemeState(t);
            apply(t);
            try {
                localStorage.setItem("theme", t);
            } catch {}
        },
        [apply]
    );

    const toggleTheme = useCallback(() => {
        setTheme(theme === "dark" ? "light" : "dark");
    }, [theme, setTheme]);

    useEffect(() => {
        try {
            const saved = localStorage.getItem("theme") as Theme | null;
            if (saved === "light" || saved === "dark") {
                setTheme(saved);
                return;
            }
        } catch {}
        const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)")
            .matches;
        setTheme(prefersDark ? "dark" : "light");
    }, [setTheme]);

    useEffect(() => {
        const mql = window.matchMedia?.("(prefers-color-scheme: dark)");
        const handler = (e: MediaQueryListEvent) => {
            try {
                if (!localStorage.getItem("theme")) {
                    setTheme(e.matches ? "dark" : "light");
                }
            } catch {}
        };
        mql?.addEventListener?.("change", handler);
        return () => mql?.removeEventListener?.("change", handler);
    }, [setTheme]);

    return (
        <ThemeContext.Provider value={{ theme, toggleTheme, setTheme }}>
            {children}
        </ThemeContext.Provider>
    );
}

export function useTheme() {
    const ctx = useContext(ThemeContext);
    if (!ctx) {
        throw new Error("useTheme must be used inside <ThemeProvider>");
    }
    return ctx;
}

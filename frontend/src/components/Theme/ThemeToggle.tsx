"use client";

import React from "react";
import styles from "./ThemeToggle.module.css";
import { useTheme } from "./ThemeProvider";

export default function ThemeToggle() {
    const { theme, toggleTheme } = useTheme();
    const isDark = theme === "dark";

    return (
        <button
            type="button"
            className={`${styles.switch} ${isDark ? styles.active : ""}`}
            onClick={toggleTheme}
            role="switch"
            aria-checked={isDark}
            aria-label="Przełącz motyw"
        >
            <span className={styles.knob} />
        </button>
    );
}


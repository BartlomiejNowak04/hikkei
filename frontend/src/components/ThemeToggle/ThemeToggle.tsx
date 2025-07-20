import React, { useState } from 'react';
import styles from './ThemeToggle.module.css';

interface ThemeToggleProps {
    toggleTheme: () => void;
}

const ThemeToggle: React.FC<ThemeToggleProps> = ({ toggleTheme }) => {
    const [isDark, setIsDark] = useState(false);

    const handleToggle = () => {
        setIsDark(!isDark);
        toggleTheme();
    };

    return (
        <div className={styles.switch} onClick={handleToggle} role="switch" aria-checked={isDark} tabIndex={0}>
            <div className={`${styles.slider} ${isDark ? styles.dark : ''}`}></div>
        </div>
    );
};

export default ThemeToggle;

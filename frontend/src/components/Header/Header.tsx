import React from 'react';
import styles from './Header.module.css';
import Logo from '/public/icons/kiwi-bird-solid.svg';
import SearchIcon from '/public/icons/search-solid.svg';
import ToggleIcon from '/public/icons/toggle-on-solid.svg';
import LanguageIcon from '/public/icons/language-solid.svg'

const Header = () => {
    return (
        <header className={styles.header}>
            <div className={styles.leftSection}>
                <Logo className={styles.logo} />
                <span className={styles.title}>HIKKEI</span>
                <div className={styles.linksContainer}>
                    <a href="#" className={styles.link}>Utwórz konto</a>
                    <a href="#" className={styles.link}>Zaloguj się</a>
                </div>
            </div>
            <div className={styles.rightSection}>
                <button className={styles.playButton}>GRAJ</button>
                <button className={styles.iconButton}>
                    <SearchIcon className={styles.iconSvg} />
                {/* placeholder for Searchbar*/}
                </button>
                <button className={styles.toggleButton}>
                    <ToggleIcon className={styles.iconSvg}/>
                {/* placeholder for ThemeToggle */}
                </button>
                <button className={styles.iconButton}>
                    <LanguageIcon className={styles.iconSvg}/>
                {/* placeholder for LanguageSwitch*/}
                </button>
            </div>
        </header>
    );
};

export default Header;

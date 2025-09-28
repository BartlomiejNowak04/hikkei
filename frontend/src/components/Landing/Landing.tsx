import Logo from '/src/icons/kiwi-bird-solid.svg';
import styles from "./Landing.module.css";
import React from "react";
import SearchBar from "@/components/SearchBar/Searchbar";

const LOGO_SRC = "/icons/kiwi-bird-solid.svg";

export default function Landing() {
    return (
        <section className={styles.section}>
            <h1 className={styles.hero}>
                <span>Witaj w</span>
                <span className={styles.brand}>
                <img
                    src={LOGO_SRC}
                    className={styles.logo}
                    alt="Hikkei logo"
                    role="img"
                    aria-label="Hikkei logo"
                />
          <span className={styles.title}>HIKKEI!</span>
        </span>
            </h1>

            <p className={styles.text}>
                Jeśli chcesz, wyszukaj artykuł w polu poniżej.
                <br />
                Masz konto? <a href="/auth" className={styles.link}>Zaloguj się</a>.{" "}
                Nie masz konta? <a href="/register" className={styles.link}>Zarejestruj się</a>.
            </p>
            <SearchBar autoFocus />
        </section>
    );
}

"use client";
import React from "react";
import styles from "./Sidebar.module.css";

type Props = {
    position: "left" | "right";
    isLogged: boolean;
    user?: { name?: string };
};

export default function Sidebar({ position, isLogged, user }: Props) {
    if (position === "left") {
        return (
            <aside className={styles.box}>
                <h2 className={styles.title}>Nawigacja</h2>
                <ul className={styles.list}>
                    <li className={styles.listItem}>
                        <a href="/articles" className={styles.link}>Artykuły</a>
                    </li>

                    {isLogged ? (
                        <>
                            <li className={styles.listItem}>
                                <a href="/profile" className={styles.link}>Mój profil</a>
                            </li>
                            <li className={styles.listItem}>
                                <a href="/settings" className={styles.link}>Moje artykuły</a>
                            </li>
                            <li className={styles.listItem}>
                                <a href="/logout" className={styles.link}>Wyloguj</a>
                            </li>
                        </>
                    ) : (
                        <p className={styles.text}></p>
                    )}
                </ul>
            </aside>
        );
    }

    // prawy
    return (
        <aside className={styles.box}>
            <h2 className={styles.title}>
                {isLogged ? `Witaj, ${user?.name ?? "Użytkowniku"}!` : ""}
            </h2>

            {isLogged ? (
                <>
                    <p className={styles.text}>Szybkie akcje:</p>
                    <ul className={styles.list}>
                        <li className={styles.listItem}>
                            <a href="/articles/new" className={styles.link}>+ Nowy artykuł</a>
                        </li>
                        <li className={styles.listItem}>
                            <a href="/drafts" className={styles.link}>Szkice</a>
                        </li>
                    </ul>
                </>
            ) : (
                <>
                    <p className={styles.text}>
                        Jesteś gościem. Zaloguj się, aby tworzyć i zapisywać artykuły.
                    </p>
                </>
            )}
        </aside>
    );
}

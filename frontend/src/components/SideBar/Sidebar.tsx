"use client";
import React from "react";
import styles from "./Sidebar.module.css";

type Props = {
    position: "left" | "right";
    isLogged: boolean;
    user?: { name?: string; avatar?: string };
};

export default function Sidebar({ position, isLogged, user }: Props) {
    if (position === "left") {
        return (
            <aside className={styles.box}>
                {isLogged ? (
                    <>
                        <div className={styles.profile}>
                            <img
                                src={user?.avatar || "/icons/kiwi-bird-solid.svg"}
                                alt={user?.name || "Avatar"}
                                className={styles.avatar}
                            />
                            <div className={styles.username}>{user?.name || "Użytkownik"}</div>
                        </div>
                        <h2 className={styles.title}>Moje konto</h2>
                        <ul className={styles.list}>
                            <li><a className={styles.link} href="/account">Edytuj konto</a></li>
                            <li><a className={styles.link} href="/my-articles">Moje artykuły</a></li>
                            <li><a className={styles.link} href="/logout">Wyloguj</a></li>
                        </ul>
                    </>
                ) : (
                    <>
                        <h2 className={styles.title}>Nawigacja</h2>
                        <p className={styles.text}>Jesteś gościem. Zaloguj się, aby tworzyć artykuły.</p>
                        <a className={styles.link} href="/auth">Zaloguj się</a>
                    </>
                )}
            </aside>
        );
    }

    // prawy
    return (
        <aside className={styles.box}>
            <h2 className={styles.title}>Akcje</h2>
            {isLogged ? (
                <ul className={styles.list}>
                    <li><a className={styles.link} href="/articles/new">Dodaj artykuł</a></li>
                    <li><a className={styles.link} href="/articles/edit">Edytuj artykuł</a></li>
                    <li><a className={styles.link} href="/categories">Kategorie</a></li>
                    <li><a className={styles.link} href="/comments">Komentarze</a></li>
                </ul>
            ) : (
                <p className={styles.text}>Zaloguj się, by dodawać i edytować.</p>
            )}
        </aside>
    );
}

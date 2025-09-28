"use client";
import React, {useEffect, useState} from "react";
import styles from "./Sidebar.module.css";
import {User} from "@/lib/types";
import {getUser} from "@/lib/ApiCaller";

type Props = {
    position: "left" | "right";
};

export default function Sidebar({ position}: Props) {
    const [isLogged, setIsLogged] = useState<boolean>(false);
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
        (async () => {
            const val = await getUser();
            setUser(val);
            setIsLogged(Boolean(val));
        })();
    }, []);

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
                            <li><a className={styles.link} href="/api/auth/logout">Wyloguj</a></li>
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

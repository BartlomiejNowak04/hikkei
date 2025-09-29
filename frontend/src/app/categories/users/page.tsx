"use client"

import {useRouter, useSearchParams} from "next/navigation";
import React, {Suspense, useEffect, useState} from "react";
import {getCategory, getUserByName, getUsersWithPermission, setPermission} from "@/lib/ApiCaller";
import utilStyles from "@/lib/util.module.css";
import styles from "@/components/Category/CategoryManager.module.css";
import {User} from "@/lib/types";

function UsersPermissionsContent() {
    const searchParams = useSearchParams();
    const id = parseInt(searchParams.get("id") ?? "0");
    const router = useRouter();
    const [name, setName] = useState<string>("");
    const [user, setUser] = useState<string>("");
    const [perm, setPerm] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [initializing, setInitializing] = useState<boolean>(true);
    const [users, setUsers] = useState<User[]>([]);

    const permissions = ['NO_ACCESS', 'UNTRUSTED', 'TRUSTED', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN'];

    useEffect(() => {

        (async () => {
            const category = await getCategory(id);
            if (!category) {
                router.push("/");
            }

            setName(category?.name ?? "");

            try {
                const users = await getUsersWithPermission(id);
                if (users) setUsers(users);
            } catch {
                setError("Nie udało się pobrać użytkowników");
            } finally {
                setInitializing(false);
            }


        })();

    }, [initializing]);

    async function handleSubmit(e : React.FormEvent) {
        e.preventDefault();

        try {
            const us = await getUserByName(user);
            if (!us) throw new Error();
            await setPermission(us.id, id, perm);
            setError(null);
            setInitializing(false);
        } catch {
            setError("Nie udało się zmienić uprawnień");
        }
    }

    async function remove(  uid: number) {
        try {
            await setPermission(uid, id, "null");
            setInitializing(false);
        } catch {
            setError("Nie udało sie zmienić uprawnień");
        }
    }

    return (
        <>
            <h1 className={utilStyles.article_title}>{name}</h1>

            <section className={styles.wrapper}>

                <div className={utilStyles.list}>
                    <h2 className={styles.sectionTitle}>
                        Dodaj użytkownika
                    </h2>

                    <form onSubmit={handleSubmit} className={utilStyles.list} id={"permUsr"}>
                        <input
                            type="search"
                            className={styles.input}
                            placeholder="Nazwa użytkownika"
                            value={user}
                            onChange={(e) => setUser(e.target.value)}
                            aria-label="Użytkowicy"
                        />
                        <select
                            id="perm"
                            className={styles.select}
                            value={perm}
                            onChange={(e) => setPerm(e.target.value)}
                            required
                        >
                            {permissions.map((p) => (
                                <option key={p} value={p}>{p}</option>
                            ))}
                        </select>
                    </form>

                    <div className={styles.actions}>
                        <button type="submit" className={styles.buttonPrimary} form={"permUsr"}>
                            {"Zmień uprawnienia"}
                        </button>
                    </div>
                </div>
                {error && (
                    <p className={styles.error} role="alert" aria-live="polite">
                        {error}
                    </p>
                )}
                <br/>


                {initializing ? (
                    <div className={styles.skeletonList} aria-hidden="true">
                        <div className={styles.skeletonItem} />
                        <div className={styles.skeletonItem} />
                        <div className={styles.skeletonItem} />
                    </div>
                ) : users.length === 0 ? (
                    <p className={styles.muted}>Brak użytkowników z uprawnieniami.</p>
                ) : (
                    // ⬇️ ZAMIANA: gridList → longList
                    <ul className={styles.longList} role="list">
                        {users.map((cat) => {
                            return (
                                // ⬇️ ZAMIANA: cardRow → longItem
                                <li key={cat.id} className={styles.longItem}>
                                    {(
                                        <div className={styles.row}>
                                            <span
                                                className={styles.categoryName}
                                                title={cat.username}
                                                aria-label={`Użytkownik: ${cat.username}`}
                                            >
                                                {cat.username}
                                            </span>
                                            <div className={styles.rowActions}>
                                                <button
                                                    type="button"
                                                    className={styles.buttonDanger}
                                                    onClick={() => remove(cat.id)}
                                                >
                                                    Usuń
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                </li>
                            );
                        })}
                    </ul>
                )}

            </section>
        </>
    );
}

export default function UsersPermissions() {
    return (
        <Suspense>
            <UsersPermissionsContent/>
        </Suspense>
    );
}

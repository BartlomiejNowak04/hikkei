"use client";
import React, { useCallback, useEffect, useMemo, useState } from "react";
import styles from "./Sidebar.module.css";
import { User } from "@/lib/types";
import { getUser } from "@/lib/ApiCaller";

type Props = {
    position: "left" | "right";
};

export default function Sidebar({ position }: Props) {
    const [isLogged, setIsLogged] = useState<boolean>(false);
    const [user, setUser] = useState<User | null>(null);
    const [pfpBust, setPfpBust] = useState<string>(""); // cache-buster dla avatara

    const displayName = useMemo(() => pickDisplayName(user), [user]);
    const avatarSrc = useMemo(() => pickAvatar(user, pfpBust), [user, pfpBust]);

    const fetchUser = useCallback(async () => {
        try {
            const val = await getUser(); // powinno zwrócić null gdy niezalogowany
            setUser(val ?? null);
            setIsLogged(Boolean(val));
            // jeżeli backend zwraca coś w stylu pfpUpdatedAt / updatedAt / avatarVersion, dołóżmy jako buster:
            // @ts-ignore
            const v = val?.pfpUpdatedAt ?? val?.avatarUpdatedAt ?? val?.avatarVersion;
            if (v) setPfpBust(`?_=${encodeURIComponent(String(v))}`);
        } catch {
            setUser(null);
            setIsLogged(false);
        }
    }, []);

    // initial fetch
    useEffect(() => { fetchUser(); }, [fetchUser]);

    // odśwież przy powrocie do zakładki / focus
    useEffect(() => {
        const onFocus = () => fetchUser();
        const onVis = () => { if (document.visibilityState === "visible") fetchUser(); };
        window.addEventListener("focus", onFocus);
        document.addEventListener("visibilitychange", onVis);
        return () => {
            window.removeEventListener("focus", onFocus);
            document.removeEventListener("visibilitychange", onVis);
        };
    }, [fetchUser]);

    // cross-tab sync (po login/logout ustaw localStorage('authChanged', Date.now()))
    useEffect(() => {
        const onStorage = (e: StorageEvent) => { if (e.key === "authChanged") fetchUser(); };
        window.addEventListener("storage", onStorage);
        return () => window.removeEventListener("storage", onStorage);
    }, [fetchUser]);

    // lokalny event bus (opcjonalnie: window.dispatchEvent(new Event("auth:changed")))
    useEffect(() => {
        const listener = () => fetchUser();
        window.addEventListener("auth:changed", listener as EventListener);
        return () => window.removeEventListener("auth:changed", listener as EventListener);
    }, [fetchUser]);

    if (position === "left") {
        return (
            <aside className={styles.box}>
                {isLogged ? (
                    <>
                        <div className={styles.profile}>
                            <img
                                src={avatarSrc}
                                alt={displayName || "Avatar"}
                                className={styles.avatar}
                                onError={(e) => {
                                    // bez zapętlania: tylko raz podmień na kiwi
                                    const img = e.currentTarget as HTMLImageElement;
                                    if (!img.dataset.fallback) {
                                        img.dataset.fallback = "1";
                                        img.src = "/icons/kiwi-bird-solid.svg";
                                    }
                                }}
                            />
                            <div className={styles.username}>{displayName || "Użytkownik"}</div>
                        </div>

                        <h2 className={styles.title}>Moje konto</h2>
                        <ul className={styles.list}>
                            <li><a className={styles.link} href="/account">Edytuj konto</a></li>
                            <li>
                                <a
                                    className={styles.link}
                                    href="/api/auth/logout"
                                    onClick={() => {
                                        try {
                                            localStorage.setItem("authChanged", String(Date.now()));
                                            window.dispatchEvent(new Event("auth:changed"));
                                        } catch {}
                                    }}
                                >
                                    Wyloguj
                                </a>
                            </li>
                        </ul>
                    </>
                ) : (
                    <>
                        <h2 className={styles.title}>Nawigacja</h2>
                        <p className={styles.text}>Jesteś gościem. Zaloguj się, aby tworzyć artykuły.</p>
                        <a
                            className={styles.link}
                            href="/auth"
                            onClick={() => {
                                // w callbacku logowania zrób to samo: localStorage('authChanged') + event
                            }}
                        >
                            Zaloguj się
                        </a>
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
                    <li><a className={styles.link} href="/categories">Kategorie</a></li>
                    <li><a className={styles.link} href="/comments">Komentarze</a></li>
                </ul>
            ) : (
                <p className={styles.text}>Zaloguj się, by dodawać i edytować.</p>
            )}
        </aside>
    );
}

/* ================= helpers ================= */

function isRenderableUrl(s: unknown): s is string {
    if (typeof s !== "string" || !s) return false;
    return /^https?:\/\//i.test(s) || /^data:/i.test(s) || s.startsWith("/");
}

function pickAvatar(user: User | null, buster: string): string {
    if (!user) return "/icons/kiwi-bird-solid.svg";

    // możliwe pola z providera (GitHub/Google itd.)
    // dostosuj, jeśli Twoje API używa innych nazw
    const providerAvatar =
        // @ts-ignore
        user.providerAvatar ??
        // @ts-ignore
        user.avatarUrl ??
        // @ts-ignore
        user.picture ??
        // @ts-ignore (czasem backend wrzuca w 'avatar' cokolwiek)
        user.avatar;

    if (isRenderableUrl(providerAvatar)) {
        return providerAvatar!;
    }

    // preferuj lokalny pfp z backendu, jeśli mamy id
    // @ts-ignore
    if (user.id != null) {
        // @ts-ignore – jeśli backend zwraca wersjonowanie avatara, można je dołączyć do bustera
        return `/api/user/pfp/${user.id}${buster || ""}`;
    }

    return "/icons/kiwi-bird-solid.svg";
}

function pickDisplayName(user: User | null): string {
    if (!user) return "";
    // najczęstsze pola z nazwą (zależnie od providera/backendu)
    // @ts-ignore
    const candidates = [user.name, user.username, user.login, user.nickname, user.displayName];
    for (const c of candidates) {
        if (typeof c === "string" && c.trim()) return c;
    }
    // spróbuj wyciągnąć z emaila (przed @), jeśli jest
    // @ts-ignore
    const email: unknown = user.email;
    if (typeof email === "string" && email.includes("@")) {
        return email.split("@")[0];
    }
    // ostateczny fallback
    return "Użytkownik";
}

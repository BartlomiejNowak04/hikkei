"use client";
import { useEffect, useState } from "react";
import styles from "./Auth.module.css";
import Link from "next/link";

type RawMethod = string | { method?: string; name?: string; id?: string; slug?: string };

function normalize(m: RawMethod) {
    if (typeof m === "string") return { key: m, label: m };
    const key = m.method ?? m.slug ?? m.id ?? m.name ?? "unknown";
    const label = m.name ?? m.method ?? m.slug ?? key;
    return { key, label };
}

export default function AuthMethodsPage({ mode }: { mode: "login" | "register" }) {
    const [methods, setMethods] = useState<Array<{ key: string; label: string }>>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            try {
                const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE}/api/auth/methods`, {
                    cache: "no-store",
                });
                if (!res.ok) {
                    const body = await res.text().catch(() => "");
                    console.error("GET /api/auth/methods failed:", res.status, body);
                    throw new Error(String(res.status));
                }
                const data: RawMethod[] = await res.json();
                setMethods(data.map(normalize));
            } catch {
                setError("Nie udało się pobrać metod.");
            } finally {
                setLoading(false);
            }
        })();
    }, []);

    if (loading) {
        return (
            <main className={styles.container}>
                <h1>Ładowanie metod…</h1>
                <ul className={styles.list}>
                    <li className={styles.skeletonBtn} />
                    <li className={styles.skeletonBtn} />
                    <li className={styles.skeletonBtn} />
                </ul>
            </main>
        );
    }

    if (error) {
        return (
            <main className={styles.errorContainer}>
                <h1 className={styles.errorTitle}>
                    Nie udało się pobrać metod {mode === "login" ? "logowania" : "rejestracji"}.
                </h1>
                <img src="/icons/sad.gif" alt="Sad gif" className={styles.errorGif} />
                <p style={{ opacity: 0.7, marginTop: 8, fontSize: 12 }}>
                    Sprawdź konfigurację proxy (rewrites) w Next.js.
                </p>
            </main>
        );
    }

    const apiBase = process.env.NEXT_PUBLIC_API_BASE;

    return (
        <main className={styles.container}>
            <h1>{mode === "login" ? "Zaloguj się" : "Zarejestruj się"}</h1>
            <p>Wybierz metodę:</p>
            <ul className={styles.list}>
                {methods.map((m) => (
                    <li key={m.key}>
                        {/*<Link href={`/backend/auth/login/admin`}>{m.label}</Link><br/>*/}
                        <a href={`${process.env.NEXT_PUBLIC_API_BASE}/api/auth/${mode}/${encodeURIComponent(m.key)}`}>
                            {m.label}
                        </a>
                    </li>
                ))}
            </ul>
        </main>
    );
}

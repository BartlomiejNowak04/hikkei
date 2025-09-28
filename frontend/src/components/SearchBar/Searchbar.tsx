"use client";

import { useEffect, useRef, useState } from "react";
import styles from "./Searchbar.module.css";

type Article = { id: number; title: string; url: string };

const API = process.env.NEXT_PUBLIC_API_BASE ?? "";
const SEARCH_BASE = `${API}/api/page/search`;

export default function SearchBar({
                                      autoFocus = false,
                                      placeholder = "Szukaj artykułu…",
                                      onClose,
                                  }: {
    autoFocus?: boolean;
    placeholder?: string;
    onClose?: () => void;
}) {
    const [q, setQ] = useState("");
    const [loading, setLoading] = useState(false);
    const [results, setResults] = useState<Article[]>([]);
    const [error, setError] = useState<string | null>(null);
    const abortRef = useRef<AbortController | null>(null);
    const inputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (autoFocus) inputRef.current?.focus();
    }, [autoFocus]);

    // debounce
    useEffect(() => {
        const t = setTimeout(() => {
            void runSearch(q);
        }, 300);
        return () => clearTimeout(t);
    }, [q]);

    const runSearch = async (term: string) => {
        const trimmed = term.trim();
        if (!trimmed) {
            setResults([]);
            setError(null);
            return;
        }

        setLoading(true);
        setError(null);
        abortRef.current?.abort();
        const ac = new AbortController();
        abortRef.current = ac;

        try {
            const res = await fetch(`${SEARCH_BASE}/${encodeURIComponent(trimmed)}`, {
                credentials: "include",
                cache: "no-store",
                signal: ac.signal,
            });
            if (!res.ok) throw new Error(String(res.status));
            const data: Article[] = await res.json();
            setResults(data ?? []);
        } catch (e: any) {
            if (e?.name !== "AbortError") setError("Nie udało się wyszukać.");
        } finally {
            setLoading(false);
        }
    };

    const onKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Escape") {
            setQ("");
            setResults([]);
            onClose?.();
        }
        if (e.key === "Enter" && results[0]) {
            window.location.href = `/articles/${encodeURIComponent(results[0].title)}`;
        }
    };

    return (
        <div className={styles.wrapper}>
            <input
                ref={inputRef}
                className={styles.input}
                placeholder={placeholder}
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyDown={onKeyDown}
                aria-label="Wyszukaj artykuł"
            />
            {loading && <div className={styles.status}>Szukam…</div>}
            {error && <div className={styles.error}>{error}</div>}

            {results.length > 0 && (
                <ul className={styles.dropdown}>
                    {results.map((a) => (
                        <li key={a.id} className={styles.item}>
                            <a href={`/articles/${encodeURIComponent(a.title)}`} onClick={onClose}>
                                {a.title}
                            </a>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

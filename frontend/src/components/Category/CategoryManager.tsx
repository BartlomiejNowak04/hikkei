"use client";
import { useEffect, useMemo, useState } from "react";
import styles from "./CategoryManager.module.css";

export type Category = {
    id: number;
    name: string;
};

async function request<T = unknown>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, init);

    if (!res.ok) {
        let msg = `HTTP ${res.status}`;
        try {
            const text = await res.text();
            if (text) msg += `: ${text}`;
        } catch {}
        throw new Error(msg);
    }

    if (res.status === 204) {
        return undefined as T;
    }

    const ct = (res.headers.get("content-type") || "").toLowerCase();

    if (!ct.includes("application/json")) {
        const text = await res.text();
        return (text ? (text as unknown as T) : (undefined as T));
    }

    const raw = await res.text();
    if (!raw) return undefined as T;

    try {
        return JSON.parse(raw) as T;
    } catch {
        return raw as unknown as T;
    }
}

export default function CategoryManager() {
    const [name, setName] = useState("");
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [initializing, setInitializing] = useState(true);

    const [editingId, setEditingId] = useState<number | null>(null);
    const [editingName, setEditingName] = useState("");
    const [query, setQuery] = useState("");

    useEffect(() => {
        (async () => {
            try {
                const data = await request<Category[]>("/api/category/all");
                setCategories(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Nie udało się pobrać kategorii:", err);
                setError("Nie udało się pobrać kategorii.");
            } finally {
                setInitializing(false);
            }
        })();
    }, []);

    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase();
        if (!q) return categories;
        return categories.filter((c) => c.name.toLowerCase().includes(q));
    }, [categories, query]);

    async function handleCreate(e: React.FormEvent) {
        e.preventDefault();
        if (!name.trim()) return;

        setLoading(true);
        setError(null);

        try {
            const created = await request<Category>("/api/category/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name }),
            });

            if (created && typeof created === "object") {
                setCategories((prev) => [created, ...prev]);
            }
            setName("");
        } catch (err) {
            console.error(err);
            setError("Nie udało się utworzyć kategorii.");
        } finally {
            setLoading(false);
        }
    }

    function startEdit(cat: Category) {
        setEditingId(cat.id);
        setEditingName(cat.name);
    }

    async function saveEdit(id: number) {
        if (!editingName.trim()) return;
        setLoading(true);
        setError(null);
        try {
            await request<void>("/api/category/update/name", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id, name: editingName }),
            });

            setCategories((prev) =>
                prev.map((c) => (c.id === id ? { ...c, name: editingName } : c)),
            );
            setEditingId(null);
            setEditingName("");
        } catch (err) {
            console.error(err);
            setError("Nie udało się zmienić nazwy kategorii.");
        } finally {
            setLoading(false);
        }
    }

    async function remove(id: number) {
        if (!confirm("Na pewno usunąć tę kategorię?")) return;
        setLoading(true);
        setError(null);
        try {
            await request<void>("/api/category/delete", {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id }),
            });
            setCategories((prev) => prev.filter((c) => c.id !== id));
        } catch (err) {
            console.error(err);
            setError("Nie udało się usunąć kategorii.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className={styles.wrapper}>
            <h1 className={styles.pageTitle}>Dodaj nową kategorię</h1>

            <form onSubmit={handleCreate} className={styles.card} aria-labelledby="add-category">
                <label className={styles.label} htmlFor="categoryName" id="add-category">
                    Nazwa kategorii
                </label>
                <div className={styles.inputRow}>
                    <input
                        id="categoryName"
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                        className={styles.inputLg}
                        placeholder="np. Programowanie"
                        aria-invalid={!!error}
                    />
                    <button type="submit" className={styles.buttonPrimary} disabled={loading}>
                        {loading ? "Zapisywanie…" : "Utwórz"}
                    </button>
                </div>
                {error && (
                    <p className={styles.error} role="alert" aria-live="polite">
                        {error}
                    </p>
                )}
            </form>

            <div className={styles.toolbar}>
                <h2 className={styles.sectionTitle}>
                    Lista kategorii
                    <span className={styles.counterBadge}>{filtered.length}</span>
                </h2>
                <input
                    type="search"
                    className={styles.input}
                    placeholder="Filtruj…"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    aria-label="Filtruj kategorie"
                />
            </div>

            {initializing ? (
                <div className={styles.skeletonList} aria-hidden="true">
                    <div className={styles.skeletonItem} />
                    <div className={styles.skeletonItem} />
                    <div className={styles.skeletonItem} />
                </div>
            ) : filtered.length === 0 ? (
                <p className={styles.muted}>Brak kategorii.</p>
            ) : (
                // ⬇️ ZAMIANA: gridList → longList
                <ul className={styles.longList} role="list">
                    {filtered.map((cat) => {
                        const isEditing = editingId === cat.id;
                        return (
                            // ⬇️ ZAMIANA: cardRow → longItem
                            <li key={cat.id} className={styles.longItem}>
                                {isEditing ? (
                                    <div className={styles.row}>
                                        <input
                                            className={styles.inputLg}
                                            value={editingName}
                                            onChange={(e) => setEditingName(e.target.value)}
                                            autoFocus
                                        />
                                        <div className={styles.rowActions}>
                                            <button
                                                type="button"
                                                className={styles.buttonPrimary}
                                                onClick={() => saveEdit(cat.id)}
                                                disabled={loading}
                                            >
                                                Zapisz
                                            </button>
                                            <button
                                                type="button"
                                                className={styles.buttonGhost}
                                                onClick={() => {
                                                    setEditingId(null);
                                                    setEditingName("");
                                                }}
                                            >
                                                Anuluj
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className={styles.row}>
              <span
                  className={styles.categoryName}
                  title={cat.name}
                  aria-label={`Kategoria: ${cat.name}`}
              >
                {cat.name}
              </span>
                                        <div className={styles.rowActions}>
                                            <button
                                                type="button"
                                                className={styles.buttonGhost}
                                                onClick={() => startEdit(cat)}
                                            >
                                                Edytuj
                                            </button>
                                            <button
                                                type="button"
                                                className={styles.buttonDanger}
                                                onClick={() => remove(cat.id)}
                                                disabled={loading}
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
    );
}


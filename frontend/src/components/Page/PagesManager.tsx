import {useEffect, useMemo, useState} from "react";
import {getPagesByCategory} from "@/lib/ApiCaller";
import {Page} from "@/lib/types";
import styles from '../Category/CategoryManager.module.css';
import utilStyles from '@/lib/util.module.css';
import {removePage} from "@/lib/deleteCaller";

export default function PagesManager({id} : {id : string | null}) {
    const [query, setQuery] = useState("");

    const [pages, setPages] = useState<Page[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [initializing, setInitializing] = useState(true);

    useEffect(() => {
        (async () => {
            try {

                const data = await getPagesByCategory(id);
                setPages(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Nie udało się pobrać artykułów:", err);
                setError("Nie udało się pobrać artykułów.");
            } finally {
                setInitializing(false);
            }
        })();
    }, [initializing]);

    const filtered = useMemo(() => {
        const q = query.trim().toLowerCase();
        if (!q) return pages;
        return pages.filter((c) => c.title.toLowerCase().includes(q));
    }, [pages, query]);

    async function remove(id: number) {
        if (!confirm("Na pewno usunąć ten artykuł?")) return;
        setError(null);
        try {
            await removePage(id);
            setPages((prev) => prev.filter((c) => c.id !== id));
        } catch (err) {
            console.error(err);
            setError("Nie udało się usunąć artykułu.");
        }
    }

    return (
        <section className={styles.wrapper}>

            <div className={styles.toolbar}>
                <h2 className={styles.sectionTitle}>
                    Lista artykułów
                    <span className={styles.counterBadge}>{filtered.length}</span>
                </h2>
                <input
                    type="search"
                    className={styles.input}
                    placeholder="Filtruj…"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    aria-label="Filtruj artykuły"
                />
            </div>

            {initializing ? (
                <div className={styles.skeletonList} aria-hidden="true">
                    <div className={styles.skeletonItem} />
                    <div className={styles.skeletonItem} />
                    <div className={styles.skeletonItem} />
                </div>
            ) : filtered.length === 0 ? (
                <p className={styles.muted}>Brak artykułów.</p>
            ) : (
                <ul className={styles.longList} role="list">
                    {filtered.map((cat) => {
                        return (
                            <li key={cat.id} className={styles.longItem}>
                                {(
                                    <div className={styles.row}>
                                        <a
                                            className={styles.categoryName}
                                            title={cat.title}
                                            aria-label={`Artykuł: ${cat.title}`}
                                            href={`/articles/${cat.title}`}
                                        >
                                            {cat.title}
                                        </a>
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

            {error && (
                <p className={styles.error} role="alert" aria-live="polite">
                    {error}
                </p>
            )}

        </section>
    )
}

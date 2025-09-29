"use client";
import { useEffect, useState } from "react";
import styles from "./NewArticle.module.css";
import { apiClient } from "@/lib/api";
import { useRouter } from "next/navigation";
import {createPage, createRevision, getCategories, getPage} from "@/lib/ApiCaller";
import {Category} from "@/lib/types";

export default function NewArticle() {
    const [title, setTitle] = useState("");
    const [category, setCategory] = useState<number | "">("");
    const [categories, setCategories] = useState<Category[]>([]);
    const [content, setContent] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    useEffect(() => {
        (async () => {
            try {
                const data = await getCategories();
                setCategories(Array.isArray(data) ? data : []);
                if (data && data.length > 0) setCategory(data[0].id);
            } catch (err) {
                console.error("Nie udało się pobrać kategorii:", err);
            }
        })();
    }, []);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        if (!title.trim() || !content.trim() || !category) return;

        setLoading(true);
        setError(null);

        try {
            const page = await createPage(title, category);
            if (!page) throw new Error();
            await createRevision(page.id, content);

            alert("Dodano artykuł!");
            router.push(`/articles/${encodeURIComponent(title.replace(" ", "_"))}`);
        } catch (err) {
            console.error(err);
            setError("Nie udało się dodać artykułu.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className={styles.wrapper}>
            <h1 className={styles.pageTitle}>Dodaj nowy artykuł</h1>

            <form onSubmit={handleSubmit} className={styles.card}>
                <div className={styles.field}>
                    <label className={styles.label} htmlFor="title">Tytuł</label>
                    <input
                        id="title"
                        type="text"
                        className={styles.inputLg}
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                        placeholder="np. Wprowadzenie do TypeScript"
                    />
                </div>

                <div className={styles.field}>
                    <label className={styles.label} htmlFor="category">Kategoria</label>
                    <select
                        id="category"
                        className={styles.select}
                        value={category}
                        onChange={(e) => setCategory(Number(e.target.value))}
                        required
                    >
                        {categories.map((cat) => (
                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                        ))}
                    </select>
                </div>

                <div className={styles.field}>
                    <label className={styles.label} htmlFor="content">Treść (Markdown)</label>
                    <textarea
                        id="content"
                        className={styles.textarea}
                        rows={14}
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        required
                        placeholder="# Nagłówek\nTreść..."
                    />
                </div>

                {error && <p className={styles.error} role="alert">{error}</p>}

                <div className={styles.actions}>
                    <button type="submit" className={styles.buttonPrimary} disabled={loading}>
                        {loading ? "Zapisywanie…" : "Dodaj artykuł"}
                    </button>
                </div>
            </form>
        </section>
    );
}

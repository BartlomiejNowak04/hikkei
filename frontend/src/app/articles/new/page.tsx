"use client";
import { useState, useEffect } from "react";
import Header from "@/components/Header/Header";
import Sidebar from "@/components/SideBar/Sidebar";
import styles from "@/app/Home.module.css";
import { apiClient } from "@/lib/api";
import { useRouter } from "next/navigation";
import { useAuth } from "@/components/Auth/AuthContext";

type Category = {
    id: number;
    name: string;
};

export default function NewArticlePage() {
    const { isLogged, user } = useAuth();
    const [title, setTitle] = useState("");
    const [category, setCategory] = useState<number | null>(null);
    const [categories, setCategories] = useState<Category[]>([]);
    const [content, setContent] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();


    useEffect(() => {
        (async () => {
            try {
                const data = await apiClient<Category[]>("/api/category/all");
                setCategories(data);
                if (data.length > 0) setCategory(data[0].id);
            } catch (err) {
                console.error("Nie udało się pobrać kategorii:", err);
            }
        })();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            // 1. page
            const page = await apiClient<{ id: number; url: string }>("/api/page/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    title,
                    category,
                }),
            });

            // revision
            await apiClient("/api/revision/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    page_id: page.id,
                    content,
                }),
            });

            alert("Dodano artykuł!");
            router.push(`/articles/${encodeURIComponent(page.url)}`);
        } catch (err) {
            console.error(err);
            setError("Nie udało się dodać artykułu.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.gridWithSidebars}>
                <Sidebar position="left" isLogged={isLogged} user={user ?? undefined} />
                <main className={styles.main}>
                    <h1>Dodaj nowy artykuł</h1>
                    <form onSubmit={handleSubmit} className={styles.form}>
                        <label className={styles.label}>
                            Tytuł
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                                className={styles.input}
                            />
                        </label>

                        <label className={styles.label}>
                            Kategoria
                            <select
                                value={category ?? ""}
                                onChange={(e) => setCategory(Number(e.target.value))}
                                className={styles.input}
                                required
                            >
                                {categories.map((cat) => (
                                    <option key={cat.id} value={cat.id}>
                                        {cat.name}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label className={styles.label}>
                            Treść (Markdown)
                            <textarea
                                value={content}
                                onChange={(e) => setContent(e.target.value)}
                                required
                                rows={12}
                                className={styles.textarea}
                            />
                        </label>

                        {error && <p className={styles.error}>{error}</p>}

                        <button type="submit" className={styles.button} disabled={loading}>
                            {loading ? "Zapisywanie…" : "Dodaj artykuł"}
                        </button>
                    </form>
                </main>
                <Sidebar position="right" isLogged={isLogged} user={user ?? undefined} />
            </div>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}

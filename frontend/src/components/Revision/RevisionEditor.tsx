"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import styles from "./RevisionEditor.module.css";
import {
    createDraft,
    createRevision,
    getDraft,
    getLogged,
    getPage,
    getRawCurrent,
    setStatus,
} from "@/lib/ApiCaller";
import { removeDraft } from "@/lib/deleteCaller";
import type { Page } from "@/lib/types";

function EditorInner() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const pageQuery = searchParams.get("page");

    const [title, setTitle] = useState<string>("[...]");
    const [content, setContent] = useState<string>("Loading previous draft...");
    const [thisPage, setThisPage] = useState<Page | null>(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        (async () => {
            if (!(await getLogged())) {
                router.push("/auth/");
                return;
            }
            if (!pageQuery) {
                router.push("/");
                return;
            }

            try {
                const page = await getPage(pageQuery);
                if (!page) throw new Error("Page not found");
                setTitle(page.title);
                setThisPage(page);

                try {
                    const d = await getDraft(page.id);
                    if (!d) throw new Error("no draft");
                    setContent(d.content);
                } catch {
                    // brak draftu – czysty edytor
                    setContent("");
                }
            } catch (e) {
                console.error("Nie udało się załadować strony: ", e);
                router.push("/");
                return;
            } finally {
                setLoading(false);
            }
        })();
    }, [router, pageQuery]);

    const handleSubmit =
        (autoApprove: boolean) =>
            async (fd: FormData) => {
                setLoading(true);
                setError(null);

                try {
                    const rev = await createRevision(thisPage?.id, fd.get("content"));

                    if (autoApprove) {
                        try {
                            await setStatus(rev?.id, true);
                            alert("Dodano i zatwierdzono rewizję!");
                        } catch (err) {
                            console.error(err);
                            alert("Dodano rewizję, lecz nie udało się jej zatwierdzić.");
                        }
                    } else {
                        alert("Dodano rewizję!");
                    }

                    router.push(`/revision?page=${encodeURIComponent(pageQuery ?? "")}`);
                } catch (err) {
                    console.error(err);
                    setError("Nie udało się dodać rewizji.");
                } finally {
                    setLoading(false);
                }
            };

    const saveDraft = async (fd: FormData) => {
        setLoading(true);
        setError(null);

        try {
            await createDraft(thisPage?.id, fd.get("content"));
            alert("Zapisano wersję roboczą!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się zapisać wersji roboczej.");
        } finally {
            setLoading(false);
        }
    };

    const loadDraft = async () => {
        setLoading(true);
        setError(null);
        try {
            const d = await getDraft(thisPage?.id);
            if (!d) throw new Error();
            setContent(d.content);
            alert("Załadowano wersję roboczą!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się załadować wersji roboczej.");
        } finally {
            setLoading(false);
        }
    };

    const loadCurrent = async () => {
        setLoading(true);
        setError(null);
        try {
            const cur = await getRawCurrent(thisPage?.id);
            if (!cur) throw new Error();
            setContent(cur.content);
            alert("Załadowano obecną rewizję!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się załadować obecnej rewizji.");
        } finally {
            setLoading(false);
        }
    };

    const deleteDraft = async () => {
        setLoading(true);
        setError(null);
        try {
            await removeDraft(thisPage?.id);
            setContent("");
            alert("Usunięto wersję roboczą!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się usunąć wersji roboczej.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className={styles.wrapper}>
            <h1 className={styles.pageTitle}>Dodaj nową rewizję dla strony {title}</h1>

            <div className={styles.card}>
                <div className={styles.toolbar}>
                    <button
                        type="button"
                        className={styles.buttonGhost}
                        onClick={loadCurrent}
                        disabled={loading}
                    >
                        {loading ? "Zapisywanie…" : "Załaduj obecną rewizję"}
                    </button>
                    <button
                        type="button"
                        className={styles.buttonGhost}
                        onClick={loadDraft}
                        disabled={loading}
                    >
                        {loading ? "Zapisywanie…" : "Załaduj wersję roboczą"}
                    </button>
                    <button
                        type="button"
                        className={styles.buttonGhost}
                        onClick={() => saveDraft(new FormData())}
                        disabled={loading}
                    >
                        Zapisz wersję roboczą
                    </button>
                    <button
                        type="button"
                        className={styles.buttonDanger}
                        onClick={deleteDraft}
                        disabled={loading}
                    >
                        Usuń wersję roboczą
                    </button>
                </div>

                <form action={handleSubmit(false)}>
                    <label className={styles.label} htmlFor="content">
                        Treść (Markdown)
                    </label>
                    <textarea
                        id="content"
                        name="content"
                        className={styles.textarea}
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        disabled={loading}
                    />

                    {error && <p className={styles.error}>{error}</p>}

                    <div className={styles.actions}>
                        <button
                            type="submit"
                            className={styles.buttonPrimary}
                            disabled={loading}
                            formAction={handleSubmit(false)}
                        >
                            {loading ? "Zapisywanie…" : "Dodaj rewizję"}
                        </button>

                        <button
                            type="submit"
                            className={styles.buttonPrimary}
                            disabled={loading}
                            formAction={handleSubmit(true)}
                        >
                            {loading ? "Zapisywanie…" : "Dodaj i zatwierdź rewizję"}
                        </button>
                    </div>
                </form>
            </div>
        </section>
    );
}

export default function NewRevisionEditor() {
    return (
        <Suspense>
            <EditorInner />
        </Suspense>
    );
}

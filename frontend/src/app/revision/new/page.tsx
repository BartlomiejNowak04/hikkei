"use client";
import {Suspense, useEffect, useState} from "react";
import styles from "@/app/Home.module.css";
import { useRouter, useSearchParams } from "next/navigation";
import {createDraft, createRevision, getDraft, getLogged, getPage, getRawCurrent, setStatus} from "@/lib/ApiCaller";
import {Page} from "@/lib/types";
import {removeDraft} from "@/lib/deleteCaller";

function NewArticleRevisionContent() {
    const router = useRouter();

    const searchParams = useSearchParams();
    const pageTitle = searchParams.get("page");

    if (pageTitle === null) {
        router.push("/");
        return null;
    }

    const [title, setTitle] = useState("[...]");
    const [content, setContent] = useState("Loading previous draft...");

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [thisPage, setThisPage] = useState<Page | null>(null);

    useEffect(() => {
        (async () => {
            if (!(await getLogged())) {
                router.push("/auth/");
            }

            try {
                const data = await getPage(pageTitle);
                if (!data) throw new Error();
                setTitle(data.title);
                setThisPage(data);

                try {
                    const content = await getDraft(data.id);
                    if (!content) throw new Error();
                    setContent(content.content);
                    setLoading(false);
                } catch (err) {
                    setContent("");
                    setLoading(false);
                }
            } catch (err) {
                console.error("Nie udało się załadować strony: ", err);
                router.push("/");
            }
        })();
    }, [router]);

    const handleSubmit = (autoApprove: boolean) => async (e: FormData) => {
        setLoading(true);
        setError(null);

        try {
            const revision = await createRevision(thisPage?.id, e.get("content"));

            if (autoApprove) {
                try {
                    await setStatus(revision?.id, true);

                    alert("Dodano i zatwierdzono rewizję!");
                } catch (err) {
                    console.error(err);
                    alert("Dodano rewizję, lecz nie udało się jej zatwierdzić.");
                }
            } else {
                alert("Dodano rewizję!");
            }
            router.push(
                `/revision?page=${pageTitle}`,
            );
        } catch (err) {
            console.error(err);
            setError("Nie udało się dodać rewizji.");
        } finally {
            setLoading(false);
        }
    };

    const saveDraft = async (e: FormData) => {
        setLoading(true);
        setError(null);

        try {
            await createDraft(thisPage?.id, e.get("content"));
            alert("Zapisano wersję roboczą!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się zapisać wersji roboczej.");
        } finally {
            setLoading(false);
        }
    };

    const loadDraft = async (e: FormData) => {
        setLoading(true);
        setError(null);

        try {
            const data = await getDraft(thisPage?.id);
            if (!data) throw new Error();
            setContent(data.content);
            alert("Załadowano wersję roboczą!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się załadować wersji roboczej.");
        } finally {
            setLoading(false);
        }
    };

    const loadCurrent = async (e: FormData) => {
        setLoading(true);
        setError(null);

        try {
            const data = await getRawCurrent(thisPage?.id);
            if (!data) throw new Error();
            setContent(data.content);
            alert("Załadowano obecną rewizję!");
        } catch (err) {
            console.error(err);
            setError("Nie udało się załadować obecnej rewizji.");
        } finally {
            setLoading(false);
        }
    };

    const deleteDraft = async (e: FormData) => {
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
        <>
            <h1>Dodaj nową rewizję dla strony {title}</h1>
            <form action={handleSubmit(false)} className={styles.form}>
                <button
                    type="submit"
                    formAction={loadCurrent}
                    className={styles.button}
                    disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Załaduj obecną rewizję"}
                </button>
                <button
                    type="submit"
                    formAction={loadDraft}
                    className={styles.button}
                    disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Załaduj wersję roboczą"}
                </button>
                <button
                    type="submit"
                    formAction={saveDraft}
                    className={styles.button}
                    disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Zapisz wersję roboczą"}
                </button>
                <button
                    type="submit"
                    formAction={deleteDraft}
                    className={styles.button}
                    disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Usuń wersję roboczą"}
                </button>

                <label className={styles.label}>
                    Treść (Markdown)
                    <textarea
                        name="content"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        rows={12}
                        className={styles.textarea}
                        disabled={loading}
                    />
                </label>

                {error && <p className={styles.error}>{error}</p>}

                <button type="submit"
                        formAction={handleSubmit(true)}
                        className={styles.button}
                        disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Dodaj rewizję"}
                </button>
                <button
                    type="submit"
                    formAction={handleSubmit(true)}
                    className={styles.button}
                    disabled={loading}
                >
                    {loading ? "Zapisywanie…" : "Dodaj i zatwierdź rewizję"}
                </button>
            </form>
        </>
    );
}

export default function NewArticleRevision() {
    return (
        <Suspense>
            <NewArticleRevisionContent/>
        </Suspense>
    )
}

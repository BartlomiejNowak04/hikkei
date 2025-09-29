// components/Article/CommentsPanel.tsx
"use client";
import { useState } from "react";

type Comment = {
    id: number;
    page_id: number;
    content: string;
    author?: { id: number; name?: string } | null;
    created_at?: string;
};

type Props = { pageId: number };

export default function CommentsPanel({ pageId }: Props) {
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);
    const [comments, setComments] = useState<Comment[] | null>(null);

    async function load() {
        setLoading(true);
        setErr(null);
        try {
            const res = await fetch(`/api/comment/page/${pageId}`, {
                credentials: "include",
                cache: "no-store",
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = (await res.json()) as Comment[] | null;
            setComments(Array.isArray(data) ? data : []);
        } catch (e: any) {
            console.error(e);
            setErr("Nie udało się pobrać komentarzy.");
        } finally {
            setLoading(false);
        }
    }

    async function toggle() {
        if (!open && comments == null) {
            await load();
        }
        setOpen(!open);
    }

    return (
        <div style={{ display: "inline-block" }}>
            <button
                type="button"
                onClick={toggle}
                style={{
                    padding: "8px 12px",
                    borderRadius: 8,
                    border: "1px solid currentColor",
                    background: "transparent",
                    cursor: "pointer",
                }}
            >
                {open ? "Ukryj komentarze" : "Pokaż komentarze"}
            </button>

            {open && (
                <div style={{ marginTop: 12 }}>
                    {loading && <p>Ładowanie…</p>}
                    {err && <p style={{ color: "crimson" }}>{err}</p>}

                    {!loading && !err && (
                        <>
                            {(!comments || comments.length === 0) ? (
                                <p>Brak komentarzy.</p>
                            ) : (
                                <ul style={{ listStyle: "none", padding: 0, margin: 0, display: "grid", gap: 8 }}>
                                    {comments.map((c) => (
                                        <li
                                            key={c.id}
                                            style={{
                                                border: "1px solid currentColor",
                                                borderRadius: 8,
                                                padding: "8px 12px",
                                                opacity: 0.9,
                                            }}
                                        >
                                            <div style={{ fontSize: 12, opacity: 0.8, marginBottom: 4 }}>
                                                {c.author?.name ?? "Anonim"} {c.created_at ? `• ${new Date(c.created_at).toLocaleString()}` : ""}
                                            </div>
                                            <div>{c.content}</div>
                                        </li>
                                    ))}
                                </ul>
                            )}

                            <div style={{ marginTop: 8 }}>
                                <button
                                    type="button"
                                    onClick={load}
                                    style={{
                                        padding: "6px 10px",
                                        borderRadius: 8,
                                        border: "1px solid currentColor",
                                        background: "transparent",
                                        cursor: "pointer",
                                    }}
                                >
                                    Odśwież
                                </button>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
}

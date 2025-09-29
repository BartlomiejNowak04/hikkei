"use client";

import { useState } from "react";
import styles from "@/components/Content/Content.module.css";
import modal from "./RevisionListClient.module.css";
import {Content} from "@/lib/types";

type Revision = {
    id: number;
    page_id: number;
    created_at?: string;
    author?: { id: number; name?: string } | null;
};

export default function RevisionListClient({
                                               apiBase,
                                               slug,
                                               pageId,
                                               revisions,
                                           }: {
    apiBase: string;
    slug: string;
    pageId: number;
    revisions: Revision[];
}) {
    const [previewId, setPreviewId] = useState<number | null>(null);
    const [previewHtml, setPreviewHtml] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    async function openPreview(id: number) {
        setPreviewId(id);
        setPreviewHtml(null);
        setLoading(true);
        setErr(null);

        try {
            // pobieramy HTML jako tekst; musi działać publicznie lub z sesją (credentials: include)
            const res = await fetch(`${apiBase}/api/revision/${id}/rendered`, {
                credentials: "include",
                cache: "no-store",
            });
            if (!res.ok) {
                const txt = await res.text().catch(() => "");
                throw new Error(`HTTP ${res.status}${txt ? `: ${txt}` : ""}`);
            }
            const html = ((await res.json()) as Content).content;
            setPreviewHtml(html);
        } catch (e: any) {
            setErr(e?.message ?? "Nie udało się pobrać podglądu.");
        } finally {
            setLoading(false);
        }
    }

    function closePreview() {
        setPreviewId(null);
        setPreviewHtml(null);
        setErr(null);
        setLoading(false);
    }

    if (revisions.length === 0) {
        return <p className={styles.muted}>Ten artykuł nie ma jeszcze żadnych rewizji.</p>;
    }

    return (
        <>
            <ul className={styles.list}>
                {revisions.map((r) => {
                    const when = r.created_at ? new Date(r.created_at).toLocaleString() : null;
                    const author = r.author?.name ?? (r.author ? `Użytkownik #${r.author.id}` : null);

                    return (
                        <li key={r.id} className={styles.listItem}>
                            <div className={styles.row}>
                                <div>
                                    <div><strong>Rewizja #{r.id}</strong></div>
                                    <div className={styles.muted}>
                                        {when ? `Utworzono: ${when}` : "Data nieznana"}
                                        {author ? ` • Autor: ${author}` : ""}
                                    </div>
                                </div>
                                <div className={styles.rowActions}>
                                    <button
                                        type="button"
                                        className={styles.buttonLink}
                                        onClick={() => openPreview(r.id)}
                                        aria-haspopup="dialog"
                                        aria-expanded={previewId === r.id}
                                    >
                                        Pokaż podgląd
                                    </button>
                                    <a
                                        className={styles.buttonLink}
                                        href={`${apiBase}/api/revision/${r.id}/rendered`}
                                        target="_blank"
                                        rel="noreferrer"
                                    >
                                        Podgląd (HTML)
                                    </a>
                                    <a
                                        className={styles.buttonLink}
                                        href={`${apiBase}/api/revision/${r.id}/raw`}
                                        target="_blank"
                                        rel="noreferrer"
                                    >
                                        Źródło (raw)
                                    </a>
                                </div>
                            </div>
                        </li>
                    );
                })}
            </ul>

            {/* Modal / panel podglądu */}
            {previewId !== null && (
                <div className={modal.overlay} role="dialog" aria-modal="true" aria-label={`Podgląd rewizji #${previewId}`}>
                    <div className={modal.panel}>
                        <div className={modal.head}>
                            <div className={modal.title}>Podgląd rewizji #{previewId}</div>
                            <button className={modal.closeBtn} onClick={closePreview} aria-label="Zamknij podgląd">×</button>
                        </div>

                        {err && <div className={styles.error}>{err}</div>}
                        {loading && <div className={styles.muted}>Wczytywanie podglądu…</div>}

                        {/* Używamy iframe srcDoc, by zachować style wygenerowanego HTML */}
                        {previewHtml && !loading && !err && (
                            <div className={modal.previewWrap}>
                                <iframe
                                    className={modal.previewIframe}
                                    // sandbox bez allow-same-origin, żeby HTML nie miał dostępu do cookies/domu
                                    sandbox=""
                                    srcDoc={previewHtml}
                                />
                            </div>
                        )}

                        <div className={modal.footer}>
                            <a
                                className={styles.buttonLink}
                                href={`${apiBase}/api/revision/${previewId}/rendered`}
                                target="_blank"
                                rel="noreferrer"
                            >
                                Otwórz w nowej karcie
                            </a>
                            <button className={styles.buttonLink} onClick={closePreview}>Zamknij</button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

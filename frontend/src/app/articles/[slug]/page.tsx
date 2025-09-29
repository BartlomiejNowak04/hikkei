// app/articles/[slug]/page.tsx
"use client"
import { notFound } from "next/navigation";
import styles from "@/components/Content/Content.module.css";
import CommentsPanel from "@/components/Article/CommentsPanel";
import {useEffect, useState} from "react";
import {getPage, getRenderedCurrent} from "@/lib/ApiCaller";
import {Page} from "@/lib/types";

// ——— Fallbackowe pobranie Page po slug ———
async function getPageBySlug(slug: string): Promise<Page | null> {
    // 1) po URL
    return await getPage(slug);
}

export default function ArticlePage({ params }: {params: Promise<{slug: string}>}) {
    const [page, setPage] = useState<Page | null>(null);
    const [html, setHtml] = useState<string | null>(null);

    useEffect(() => {
        (async () => {

            const slug = (await params).slug;
            if (!slug) notFound();

            // — 1) Page (z fallbackiem) —
            const pg = await getPageBySlug(slug);
            setPage(pg);
            if (!pg) {
                // jeżeli nic nie znaleziono -> 404
                notFound();
            }

            const html = await getRenderedCurrent(pg.id);
            setHtml(html?.content ?? "");

            // — 3) Spróbuj latest -> fallback do current (gdy 401/403/404) —
        })();
    }, []);

    return (
        <article className={styles.wrapper}>
            <a href="/articles" className={styles.backLink} aria-label="Wróć do listy artykułów">
                &larr; Wróć do listy
            </a>

            <header className={styles.headerRow}>
                <h1 className={styles.title}>{page?.title}</h1>
                <div className={styles.actionsRow}>
                    <a className={styles.buttonLink} href={`/articles/${encodeURIComponent(page?.id ?? "")}/revisions`}>
                        Zobacz wszystkie rewizje
                    </a>
                    <CommentsPanel pageId={page?.id ?? 0} />
                </div>
            </header>

            {html ? (
                <div className={styles.prose} dangerouslySetInnerHTML={{ __html: html }} />
            ) : (
                <div className={styles.prose}>
                    <p>Ten artykuł nie ma jeszcze publicznej treści.</p>
                </div>
            )}
        </article>
    );
}

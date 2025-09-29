"use client";

import { useEffect, useState } from "react";
import styles from "@/components/Content/Content.module.css";
import CommentsPanel from "@/components/Article/CommentsPanel";
import { getPage, getRenderedCurrent } from "@/lib/ApiCaller";
import type { Page } from "@/lib/types";

type Props = { slug: string };

export default function ArticleClient({ slug }: Props) {
    const [page, setPage] = useState<Page | null>(null);
    const [html, setHtml] = useState<string | null>(null);

    useEffect(() => {
        let alive = true;
        (async () => {
            try {
                const pg = await getPage(slug);
                if (!alive) return;
                setPage(pg ?? null);

                if (pg?.id != null) {
                    try {
                        const rendered = await getRenderedCurrent(pg.id);
                        if (!alive) return;
                        setHtml(rendered?.content ?? null);
                    } catch {
                        if (!alive) return;
                        setHtml(null);
                    }
                }
            } catch {
                if (!alive) return;
                setPage(null);
                setHtml(null);
            }
        })();
        return () => {
            alive = false;
        };
    }, [slug]);

    return (
        <article className={styles.wrapper}>

            <h1 className={styles.articleTitle}>{page?.title ?? ""}</h1>

            {html && <div className={styles.prose} dangerouslySetInnerHTML={{ __html: html }} />}

            <div className={styles.actionsRow}>
                <a
                    className={styles.buttonLink}
                    href={`/articles/${encodeURIComponent(slug)}/revisions`}
                >
                    Zobacz wszystkie rewizje
                </a>

                <a
                    className={styles.buttonLink}
                    href={`/revision/new?page=${encodeURIComponent(page?.title ?? "")}`}
                >
                    Edytuj artykuł
                </a>

                <CommentsPanel pageId={page?.id ?? 0} />
            </div>
        </article>
    );
}

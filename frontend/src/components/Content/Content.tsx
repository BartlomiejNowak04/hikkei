import { redirect } from "next/navigation";
import styles from "./Content.module.css";
import Landing from "@/components/Landing/Landing";
import {Page} from "@/lib/types";

type ContentProps = {
    isLogged: boolean;
    user?: { name?: string };
    pageUrl?: string;
    pageTitle?: string;
};


async function apiServerText(url: string): Promise<string> {
    const res = await fetch(url, {
        credentials: "include",
        cache: "no-store",
    });
    if (res.status === 401) redirect("/auth?reason=401");
    if (res.status === 403) redirect("/forbidden");
    if (!res.ok) {
        throw new Error(`HTTP ${res.status} for ${url}`);
    }
    return await res.text();
}

async function fetchPage(params: { pageUrl?: string; pageTitle?: string }): Promise<Page> {
    const { pageUrl, pageTitle } = params;

    // if (pageUrl) {
    //     // GET /api/page/:url
    //     return apiServer<Page>(`/api/page/${encodeURIComponent(pageUrl)}`);
    // }
    // if (pageTitle) {
    //     // GET /api/page/title/:title
    //     return apiServer<Page>(`/api/page/title/${encodeURIComponent(pageTitle)}`);
    // }
    // // GET /api/page/random
    // return apiServer<Page>("/api/page/random");
    return {id:0, category:1, title: ""};
}

async function fetchRenderedHtml(pageId: number): Promise<string> {
    // GET /api/revision/current/:id/rendered  -> text/html
    return apiServerText(`/api/revision/current/${pageId}/rendered`);
}

export default async function Content({ isLogged, user, pageUrl, pageTitle }: ContentProps) {
    if (!isLogged) {
        return <Landing />;
    }

    try {
        const page = await fetchPage({ pageUrl, pageTitle });
        const html = await fetchRenderedHtml(page.id);

        return (
            <div className={styles.wrapper}>
                <h1 className={styles.title}>{page.title}</h1>

                <p className={styles.text}>
                    <a href={`/articles/${encodeURIComponent(page.title)}`} className={styles.link}>
                        /articles/{page.title}
                    </a>
                </p>

                <article
                    className={styles.content}
                    dangerouslySetInnerHTML={{ __html: html }}
                />
            </div>
        );
    } catch {
        return (
            <div className={styles.wrapper}>
                <h1 className={styles.title}>Witaj, {user?.name ?? "użytkowniku"}!</h1>
                <p className={styles.text}>Nie udało się wczytać strony.</p>
            </div>
        );
    }
}


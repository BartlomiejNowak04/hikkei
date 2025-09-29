"use client"

import styles from "./Content.module.css";
import Landing from "@/components/Landing/Landing";
import {useEffect, useState} from "react";
import {getLogged, getRandomPage, getRenderedCurrent} from "@/lib/ApiCaller";
import {Page} from "@/lib/types";
import {titleToUrl} from "@/lib/utils";

type ContentProps = {
    user?: { name?: string };
    pageUrl?: string;
    pageTitle?: string;
};


export default function Content({user}: ContentProps) {
    const [isLogged, setIsLogged] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<Page | null>(null);
    const [content, setContent] = useState<string>("");

    useEffect(() => {
        (async () => {
            setIsLogged(await   getLogged());
            try {
                const pg = await getRandomPage();
                setPage(pg);
                if (!pg) throw new Error();
                setContent((await getRenderedCurrent(pg.id))?.content ?? "");
                setError(null);
            } catch {
                setError("");
            }
        })();
    }, []);


    if (!isLogged) {
        return <Landing />;
    }

    if (error) {
        return (
            <div className={styles.wrapper}>
                <h1 className={styles.title}>Witaj, {user?.name ?? "użytkowniku"}!</h1>
                <p className={styles.text}>Nie udało się wczytać strony.</p>
            </div>
        );
    }

    return (
        <div className={styles.wrapper}>
            <a href={`/articles/${titleToUrl(page?.title ?? "")}`}><h1 className={styles.title}>{titleToUrl(page?.title ?? "")}</h1></a>

            <article
                className={styles.content}
                dangerouslySetInnerHTML={{ __html: content ?? "" }}
            />
        </div>
    );
}


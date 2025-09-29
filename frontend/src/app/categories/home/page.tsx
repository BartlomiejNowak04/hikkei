"use client"

import {Suspense, useEffect, useState} from "react";
import {getCategory} from "@/lib/ApiCaller";
import {useRouter, useSearchParams} from "next/navigation";
import utilStyles from '@/lib/util.module.css';
import PagesManager from "@/components/Page/PagesManager";

function CategoryPageContent() {
    const searchParams = useSearchParams();
    const id = searchParams.get("id");
    const [name, setName] = useState<string>("");
    const router = useRouter();

    useEffect(() => {

        (async () => {
            const category = await getCategory(id);
            if (!category) {
                router.push("/");
            }

            setName(category?.name ?? "");
        })();

    }, []);

    return (
        <>
            <div className={utilStyles.flex}>

                <h1 className={utilStyles.article_title}>{name}</h1>
                <button className={utilStyles.button}><a href={`/categories/users?id=${id}`}>Zarządzaj dostępem</a></button>
            </div>
            <PagesManager id={id}/>
        </>
    );
}

export default function CategoryPage() {
    return (
      <Suspense>
          <CategoryPageContent/>
      </Suspense>
    );
}

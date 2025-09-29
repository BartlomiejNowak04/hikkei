"use client"

import {Suspense, useEffect, useState} from "react";
import {getCategory} from "@/lib/ApiCaller";
import {useRouter, useSearchParams} from "next/navigation";
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
            <h1>{name}</h1>
            <button>Zarządzaj dostępem</button>
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

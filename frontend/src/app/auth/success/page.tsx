"use client";
import {Suspense, useEffect} from "react";
import { useSearchParams, useRouter } from "next/navigation";

function AuthSuccessPageContent() {
    const params = useSearchParams();
    const router = useRouter();

    const mode = params.get("mode") ?? "login";

    useEffect(() => {
        router.replace("/");
    }, []);

    return (
        <p>
            {mode === "register" ? "Rejestracja zakończona sukcesem!" : "Logowanie udane."}
            {" "}Przekierowuję…
        </p>
    );
}

export default function AuthSuccessPage() {
    return (
        <Suspense>
            <AuthSuccessPageContent/>
        </Suspense>
    );
}


"use client";
import {Suspense, useEffect} from "react";
import { useRouter } from "next/navigation";

export default function AuthSuccessPage() {
    const router = useRouter();

    useEffect(() => {
        router.replace("/");
    }, []);

    return (
        <p>
            Przekierowuję…
        </p>
    );
}

"use client";
import {Suspense, useEffect} from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { useAuth } from "@/components/Auth/AuthContext";

function extractUsernameFromMessage(msg: string | null): string | null {
    if (!msg) return null;
    const m = msg.match(/logged\\s+as\\s+user\\s+([^\\s]+)/i);
    return m ? m[1] : null;
}

function AuthSuccessPageContent() {
    const params = useSearchParams();
    const router = useRouter();
    const { login } = useAuth();

    const mode = params.get("mode") ?? "login";

    useEffect(() => {
        const token: string | null = params.get("token");
        let name: string | null = params.get("username");
        const avatar: string | null = params.get("avatar");

        if (!name) {
            const message = params.get("message");
            name = extractUsernameFromMessage(message);
        }

        login(token ?? "session-cookie", {
            name: name ?? "Użytkownik",
            avatar: avatar ?? undefined,
        });

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


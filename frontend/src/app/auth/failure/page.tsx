"use client";
import { useSearchParams } from "next/navigation";
import {Suspense, useEffect} from "react";

function AuthFailurePageContent() {
    const params = useSearchParams();
    const mode = params.get("mode") ?? "login"; // login | register

    return (
        <main style={{ padding: 24 }}>
            <h1>
                {mode === "register"
                    ? "Rejestracja nieudana"
                    : "Logowanie nieudane"}
            </h1>

            <p>
                {mode === "register"
                    ? "Nie udało się założyć konta. Spróbuj ponownie albo wybierz inną metodę."
                    : "Nie udało się zalogować. Spróbuj ponownie albo wybierz inną metodę."}
            </p>

            <a href={mode === "register" ? "/register" : "/auth"}>
                Wróć do {mode === "register" ? "rejestracji" : "logowania"}
            </a>
        </main>
    );
}

export default function AuthFailurePage() {
    return (
      <Suspense>
          <AuthFailurePageContent/>
      </Suspense>
    );
}

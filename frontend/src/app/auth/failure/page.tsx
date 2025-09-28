"use client";

export default function AuthFailurePage() {

    return (
        <main style={{ padding: 24 }}>
            <p>
                Nieudana próba rejestracji/logowania.
            </p>

            <p>
                Wróć do <a href={"/register"}>rejestracji</a> lub <a href={"/auth"}>logowania</a>.
            </p>
        </main>
    );
}

import Header from "@/components/Header/Header";
import styles from "./Home.module.css";
import AuthWrapper from "@/components/Auth/AuthWrapper";

export default function Home() {
    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.gridWithSidebars}>
                <AuthWrapper position="left" />
                <main className={styles.main}>
                    {/*<Content isLogged={false} />*/}
                </main>
                <AuthWrapper position="right" />
            </div>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}


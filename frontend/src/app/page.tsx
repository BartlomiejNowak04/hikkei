import Header from "@/components/Header/Header";
import styles from "./Home.module.css";

export default function Home() {
    return (
        <div className={styles.container}>
            <Header />
            <main className={styles.main}>tu będzie coś tam coś tam ebe ebe</main>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}


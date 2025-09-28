import Header from "@/components/Header/Header";
import styles from "./Home.module.css";
import Sidebar from "@/components/SideBar/Sidebar";

export default function Home() {
    return (
        <div className={styles.container}>
            <Header />
            <div className={styles.gridWithSidebars}>
                <Sidebar position="left" />
                <main className={styles.main}>
                    {/*<Content isLogged={false} />*/}
                </main>
                <Sidebar position="right" />
            </div>
            <footer className={styles.footer}>© 2025 HIKKEI</footer>
        </div>
    );
}


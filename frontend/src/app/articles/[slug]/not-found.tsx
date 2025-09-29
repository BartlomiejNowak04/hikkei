import styles from "@/components/Content/Content.module.css";

export default function ArticleNotFound() {
    return (
        <div className={styles.wrapper}>
            <h1 className={styles.title}>Nie znaleziono artykułu</h1>
            <p className={styles.text}>Wygląda na to, że ten artykuł nie istnieje.</p>
            <a href="/articles" className={styles.link}>Przejdź do listy artykułów</a>
        </div>
    );
}

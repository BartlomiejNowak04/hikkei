// "use client";
//
// import React, {useEffect, useState} from 'react';
// import styles from './Header.module.css';
// import SearchBar from "@/components/SearchBar/Searchbar";
// import ThemeToggle from "@/components/Theme/ThemeToggle";
// import Logo from '/public/icons/kiwi-bird-solid.svg';
// import SearchIcon from '/public/icons/search-solid.svg';
// import ToggleIcon from '/public/icons/toggle-on-solid.svg';
// import LanguageIcon from '/public/icons/language-solid.svg'

// type Provider = {
//     name: string;
// };
//
// const API = process.env.NEXT_PUBLIC_API_BASE ?? "";
//
// const Header = () => {
//     const [showLoginModal, setShowLoginModal] = useState(false);
//     const [providers, setProviders] = useState<Provider[]>([]);
//
//     useEffect(() => {
//         if (showLoginModal) {
//             fetch('/auth/methods')
//                 .then(res => res.json())
//                 .then(data => setProviders(data))
//                 .catch(console.error);
//         }
//     }, [showLoginModal]);
//
//     return (
//         <header className={styles.header}>
//             <div className={styles.leftSection}>
//                 <Logo className={styles.logo} />
//                 <span className={styles.title}>HIKKEI</span>
//                 <div className={styles.linksContainer}>
//                     {/*<a href="#" className={styles.link}>Utwórz konto</a>*/}
//                     <button
//                         className={styles.link}
//                         onClick={() => setShowLoginModal(true)}
//                     >Utwórz konto </button>
//                     <button
//                         className={styles.link}
//                         onClick={() => setShowLoginModal(true)}
//                     >Zaloguj się </button>
//
//                     {/*<a href="#" className={styles.link}>Zaloguj się</a>*/}
//                 </div>
//             </div>
//             <div className={styles.rightSection}>
//                 <button className={styles.playButton}>GRAJ</button>
//                 <button className={styles.iconButton}>
//                     <SearchIcon className={styles.iconSvg} />
//                 {/* placeholder for Searchbar*/}
//                 </button>
//                 <button className={styles.toggleButton}>
//                     <ToggleIcon className={styles.iconSvg}/>
//                 {/* placeholder for ThemeToggle */}
//                 </button>
//                 <button className={styles.iconButton}>
//                     <LanguageIcon className={styles.iconSvg}/>
//                 {/* placeholder for LanguageSwitch*/}
//                 </button>
//             </div>
//
//             {showLoginModal && (
//                 <div className={styles.modalOverlay} onClick={() => setShowLoginModal(false)}>
//                     <div className={styles.modal} onClick={e => e.stopPropagation()}>
//                         <h3>Wybierz metodę logowania</h3>
//                         {providers.length === 0 && <p>Ładowanie…</p>}
//                         {providers.map(p => (
//                             <button
//                                 key={p.name}
//                                 className={styles.providerButton}
//                                 onClick={() => window.location.href = `/auth/login/${p.name}`}
//                             >
//                                 {p.name}
//                             </button>
//                         ))}
//                         <button
//                             className={styles.link}
//                             onClick={() => setShowLoginModal(false)}
//                         >
//                             Zamknij</button>
//                     </div>
//                 </div>
//             )}
//
//         </header>
//     );
// };


"use client";

import React, {useEffect, useState} from "react";
import styles from "./Header.module.css";
import SearchBar from "@/components/SearchBar/Searchbar";
import ThemeToggle from "@/components/Theme/ThemeToggle";
import {getUser} from "@/lib/ApiCaller";

const LOGO_SRC = "/icons/kiwi-bird-solid.svg";
const SEARCH_SRC = "/icons/search-solid.svg";
const LANGUAGE_SRC = "/icons/language-solid.svg";

const Header = () => {
    const [showSearch, setShowSearch] = useState(false);
    const [isLogged, setIsLogged] = useState<boolean>(false);

    useEffect(() => {
        (async () => {
            setIsLogged(Boolean(await getUser()));
        })();
    }, []);

    return (
        <header className={styles.header}>
            <div className={styles.leftSection}>
                <img
                    src={LOGO_SRC}
                    className={styles.logo}
                    alt="Hikkei logo"
                    role="img"
                    aria-label="Hikkei logo"
                />
                <span className={styles.title}>HIKKEI</span>

                {
                    !isLogged &&
                    <div className={styles.linksContainer}>
                        <a href="/register" className={styles.link}>
                            Utwórz konto
                        </a>
                        <a href="/auth" className={styles.link}>
                            Zaloguj się
                        </a>
                    </div>
                }

            </div>

            <div className={styles.rightSection}>
                <button className={styles.playButton}>GRAJ</button>

                <button
                    className={styles.iconButton}
                    onClick={() => setShowSearch((s) => !s)}
                    aria-label="Szukaj"
                >
                    <img src={SEARCH_SRC} className={styles.iconSvg} alt="" />
                </button>

                {showSearch && (
                    <div style={{ marginRight: 12 }}>
                        <SearchBar autoFocus onClose={() => setShowSearch(false)} />
                    </div>
                )}

                <ThemeToggle />

                <button className={styles.iconButton} aria-label="Język">
                    <img src={LANGUAGE_SRC} className={styles.iconSvg} alt="" />
                </button>
            </div>
        </header>
    );
};

export default Header;

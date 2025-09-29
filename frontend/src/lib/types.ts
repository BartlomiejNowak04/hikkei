export type User = {
    name: string;
    avatar?: string;
};

export type Page = {
    id: number;
    category: number;
    title: string;
};

export type Content = {
    content: string;
};

export type Id = {
    id: number;
}

export type Category = { id: number; name: string };

export type Revision = { id: number; page_id: number; created_at?: string };

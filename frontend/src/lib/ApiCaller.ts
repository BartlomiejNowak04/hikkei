import {Category, Content, Id, Page, User} from "@/lib/types";
import {apiClient} from "@/lib/api";

const API = process.env.NEXT_PUBLIC_API_BASE;

type Empty = {
};

async function getType<T = unknown>(url: string, auth: boolean, error: boolean) {
    return await apiClient<T>(url, auth, error, undefined);
}

async function createObject(url: string, body: string) {
    return await apiClient<Id>(url, true, true, {
        body: body,
        method: "POST",
    })
}

async function updateObject(url: string, body: string) {
    return await apiClient<Empty>(url, true, true, {
        body: body,
        method: "PUT",
    })
}

export async function getUser() {
    return await getType<User>(`${API}/api/user/`, false, false);
}

export async function getLogged() {
    return Boolean(await getUser());
}

export async function getPage(url : string) {
    return await getType<Page>(`${API}/api/page/${url}`, true, true);
}

export async function getDraft(id : number | undefined) {
    return await getType<Content>(`${API}/api/revision/draft/${id}`, true, true);
}

export async function getRawCurrent(id: number | undefined) {
    return await getType<Content>(`${API}/api/revision/current/${id}/raw`, true, true);
}

export async function getCategories() {
    return await getType<Category[]>(`${API}/api/category/all`, false, false);
}

export async function getCategory(id: string | null) {
    return await getType<Category>(`${API}/api/category/${id}`, true, true);
}

export async function getPagesByCategory(id: string | null) {
    return await getType<Page[]>(`${API}/api/page/category/${id}`, true, true);
}

export async function createPage(title: string, category: number) {
    return await createObject(`${API}/api/page/create`, JSON.stringify({
        title: title,
        category: category,
    }));
}

export async function createRevision(id: number | undefined, content: FormDataEntryValue | null) {
    return await createObject(`${API}/api/revision/create`, JSON.stringify({
        page_id: id,
        content: content,
    }));
}

export async function createDraft(id: number | undefined, content: FormDataEntryValue | null) {
    return await createObject(`${API}/api/revision/draft/save`, JSON.stringify({
        page: id,
        content: content,
    }));
}

export async function setStatus(id: number | undefined, status: boolean) {
    return await updateObject(`${API}/api/revision/update/status`, JSON.stringify({
        id: id,
        status: status,
    }));
}

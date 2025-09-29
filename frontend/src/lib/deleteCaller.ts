import {apiClient} from "@/lib/api";
import {Id} from "@/lib/types";

type Empty = {

};

const API = process.env.NEXT_PUBLIC_API_BASE;

async function deleteObject(url: string, body: string) {
    await apiClient<Empty>(url, true, true, {
        body: body,
        method: "DELETE",
    })
}

export async function removeDraft(id: number | undefined) {
    await deleteObject(`${API}/api/revision/draft/delete`, JSON.stringify({
        page: id,
    }));
}

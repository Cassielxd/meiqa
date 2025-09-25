
import BasicLayout from "@/components/main";

const meta = {};

const pre = "user_";

export default {
    path: "/tenant/user",
    name: "user",
    header: "user",
    redirect: {
        name: `${pre}list`
    },
    meta,
    component: BasicLayout,
    children: [
        {
            path: "list",
            name: `${pre}list`,
            meta: {
                title: "menu.userList"
            },
            component: () => import("@/pages/user/list/index")
        },
        {
            path: "group",
            name: `${pre}group`,
            meta: {
                footer: true,
                title: "menu.userGroup"
            },
            component: () => import("@/pages/user/group/index")
        },
        {
            path: "label",
            name: `${pre}label`,
            meta: {
                footer: true,
                title: "menu.userLabel"
            },
            component: () => import("@/pages/user/label/index")
        },
    ]
};
